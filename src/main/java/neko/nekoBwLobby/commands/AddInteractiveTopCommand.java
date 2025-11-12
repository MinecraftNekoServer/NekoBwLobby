package neko.nekoBwLobby.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.database.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AddInteractiveTopCommand implements CommandExecutor {
    
    private final NekoBwLobby plugin;
    private final DatabaseManager databaseManager;
    
    // 为每个玩家存储排行榜类型状态，不持久化
    private final Map<String, Map<String, TopType>> playerTopTypes = new HashMap<>();
    
    // 存储每个排行榜的盔甲架信息
    private final Map<String, TopArmorStands> topArmorStandsMap = new HashMap<>();
    
    // 排行榜类型枚举
    public enum TopType {
        SCORE("分数"),
        KILLS("击杀数"),
        DESTROYED_BEDS("挖床数"),
        DEATHS("死亡数");
        
        private final String displayName;
        
        TopType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 存储排行榜盔甲架的类
    private static class TopArmorStands {
        ArmorStand titleStand;
        ArmorStand hintStand;
        ArmorStand[] rankStands = new ArmorStand[10];
    }
    
    public AddInteractiveTopCommand(NekoBwLobby plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此指令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§c用法: /addinteractivetop <name>");
            player.sendMessage("§c创建一个可交互的排行榜悬浮字，支持点击翻页");
            return true;
        }
        
        String topName = args[0];
        
        // 获取玩家当前位置
        Location location = player.getLocation();
        
        // 保存位置到配置文件
        plugin.getConfig().set("top-locations." + topName + ".world", location.getWorld().getName());
        plugin.getConfig().set("top-locations." + topName + ".x", location.getX());
        plugin.getConfig().set("top-locations." + topName + ".y", location.getY());
        plugin.getConfig().set("top-locations." + topName + ".z", location.getZ());
        plugin.getConfig().set("top-locations." + topName + ".type", "interactive");
        plugin.saveConfig();
        
        player.sendMessage("§a成功创建可交互排行榜悬浮字: " + topName);
        player.sendMessage("§a位置: " + location.getWorld().getName() + ", " + 
                          String.format("%.2f, %.2f, %.2f", location.getX(), location.getY(), location.getZ()));
        
        // 获取排行榜数据并创建悬浮文字（默认为SCORE类型）
        createInteractiveFloatingText(location, topName);
        
        return true;
    }
    
    private void createInteractiveFloatingText(Location location, String topName) {
        // 创建一个盔甲架作为标题
        World world = location.getWorld();
        if (world == null) {
            plugin.getLogger().warning("无法在指定世界中创建悬浮字，世界为空");
            return;
        }
        
        // 获取或创建盔甲架容器
        TopArmorStands armorStands = topArmorStandsMap.computeIfAbsent(topName, k -> new TopArmorStands());
        
        // 创建标题盔甲架
        Location titleLocation = location.clone().add(0, 3.0, 0); // 标题在上方
        if (armorStands.titleStand == null || !armorStands.titleStand.isValid()) {
            armorStands.titleStand = (ArmorStand) world.spawnEntity(titleLocation, EntityType.ARMOR_STAND);
            armorStands.titleStand.setCustomName("§6=== " + TopType.SCORE.getDisplayName() + "排行榜 ===");
            armorStands.titleStand.setCustomNameVisible(true);
            armorStands.titleStand.setGravity(false);
            armorStands.titleStand.setInvulnerable(true);
            armorStands.titleStand.setVisible(false);
            armorStands.titleStand.setMarker(true); // 设置为标记，使其更容易被点击
        }
        
        // 创建提示信息盔甲架（说明如何切换排行榜）
        Location hintLocation = location.clone().add(0, 2.7, 0);
        if (armorStands.hintStand == null || !armorStands.hintStand.isValid()) {
            armorStands.hintStand = (ArmorStand) world.spawnEntity(hintLocation, EntityType.ARMOR_STAND);
            armorStands.hintStand.setCustomName("§7点击切换排行榜类型 [TOP:" + topName + "]"); // 添加特殊标识
            armorStands.hintStand.setCustomNameVisible(true);
            armorStands.hintStand.setGravity(false);
            armorStands.hintStand.setInvulnerable(true);
            armorStands.hintStand.setVisible(false);
            armorStands.hintStand.setMarker(true); // 设置为标记，使其更容易被点击
        }
        
        // 创建每个排名的盔甲架
        for (int i = 0; i < 10; i++) {
            Location armorStandLocation = location.clone().add(0, 2.5 - (i * 0.3), 0);
            if (armorStands.rankStands[i] == null || !armorStands.rankStands[i].isValid()) {
                armorStands.rankStands[i] = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
                armorStands.rankStands[i].setCustomName("§e" + (i + 1) + ". " + "加载中..." + " [TOP:" + topName + "]"); // 添加特殊标识
                armorStands.rankStands[i].setCustomNameVisible(true);
                armorStands.rankStands[i].setGravity(false);
                armorStands.rankStands[i].setInvulnerable(true);
                armorStands.rankStands[i].setVisible(false); // 不显示盔甲架本身，只显示名字
                armorStands.rankStands[i].setMarker(true); // 设为marker，便于点击
            }
        }
        
        // 为所有在线玩家更新显示内容
        updateAllPlayersDisplay(topName);
    }
    
    // 为所有在线玩家更新显示内容
    private void updateAllPlayersDisplay(String topName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updateArmorStandTextForPlayer(topName, player);
        }
    }
    
    // 为指定玩家更新盔甲架文本
    private void updateArmorStandTextForPlayer(String topName, Player player) {
        TopArmorStands armorStands = topArmorStandsMap.get(topName);
        if (armorStands == null) return;
        
        TopType topType = getPlayerTopType(player, topName);
        List<Map<String, Object>> topPlayersData = getTopPlayersData(topType, 10);
        
        // 更新标题盔甲架
        if (armorStands.titleStand != null && armorStands.titleStand.isValid()) {
            armorStands.titleStand.setCustomName("§6=== " + topType.getDisplayName() + "排行榜 ===");
        }
        
        // 更新每个排名的盔甲架
        for (int i = 0; i < 10; i++) {
            if (armorStands.rankStands[i] != null && armorStands.rankStands[i].isValid()) {
                if (i < topPlayersData.size()) {
                    Map<String, Object> playerData = topPlayersData.get(i);
                    String playerName = (String) playerData.get("name");
                    Object value = playerData.get(getValueKey(topType));
                    armorStands.rankStands[i].setCustomName("§e" + (i + 1) + ". " + playerName + " - " + value + " [TOP:" + topName + "]");
                } else {
                    armorStands.rankStands[i].setCustomName("§e" + (i + 1) + ". " + "无数据" + " [TOP:" + topName + "]");
                }
            }
        }
    }
    
    private List<Map<String, Object>> getTopPlayersData(TopType topType, int limit) {
        switch (topType) {
            case KILLS:
                return databaseManager.getTopKills(limit);
            case DESTROYED_BEDS:
                return databaseManager.getTopDestroyedBeds(limit);
            case DEATHS:
                return databaseManager.getTopDeaths(limit);
            case SCORE:
            default:
                return databaseManager.getTopPlayers(limit);
        }
    }
    
    private String getValueKey(TopType topType) {
        switch (topType) {
            case KILLS:
                return "kills";
            case DESTROYED_BEDS:
                return "destroyedBeds";
            case DEATHS:
                return "deaths";
            case SCORE:
            default:
                return "score";
        }
    }
    
    // 为指定玩家切换排行榜类型的方法
    public void switchTopTypeForPlayer(String topName, Player player) {
        // 获取玩家当前类型
        TopType currentType = getPlayerTopType(player, topName);
        
        // 计算下一个类型
        TopType nextType;
        switch (currentType) {
            case SCORE:
                nextType = TopType.KILLS;
                break;
            case KILLS:
                nextType = TopType.DESTROYED_BEDS;
                break;
            case DESTROYED_BEDS:
                nextType = TopType.DEATHS;
                break;
            case DEATHS:
                nextType = TopType.SCORE;
                break;
            default:
                nextType = TopType.SCORE;
        }
        
        // 更新玩家的排行榜类型
        String playerUUID = player.getUniqueId().toString();
        playerTopTypes.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(topName, nextType);
        
        // 为玩家更新显示内容
        updateArmorStandTextForPlayer(topName, player);
        
        // 发送消息给玩家
        player.sendMessage("§a已切换排行榜类型到: " + nextType.getDisplayName());
    }
    
    // 获取玩家的排行榜类型
    private TopType getPlayerTopType(Player player, String topName) {
        Map<String, TopType> playerTypes = playerTopTypes.get(player.getUniqueId().toString());
        if (playerTypes != null) {
            TopType type = playerTypes.get(topName);
            if (type != null) {
                return type;
            }
        }
        // 默认为分数排行榜
        return TopType.SCORE;
    }

    // 检查盔甲架是否属于指定的排行榜（用于监听器中识别盔甲架）
    public boolean isArmorStandOfTop(String topName, ArmorStand clickedArmorStand) {
        // 检查盔甲架名称是否包含指定的topName标识
        String customName = clickedArmorStand.getCustomName();
        if (customName != null) {
            return customName.contains("[TOP:" + topName + "]");
        }
        return false;
    }
    
    // 提供一个公共方法来获取数据库管理器
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    // 获取玩家的当前类型（公共方法）
    public TopType getPlayerCurrentType(Player player, String topName) {
        return getPlayerTopType(player, topName);
    }
    
    // 获取排行榜盔甲架
    public TopArmorStands getTopArmorStands(String topName) {
        return topArmorStandsMap.get(topName);
    }
}