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
import java.util.UUID;

public class AddInteractiveTopCommand implements CommandExecutor {
    
    private final NekoBwLobby plugin;
    private final DatabaseManager databaseManager;
    
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
        
        // 获取排行榜数据并创建悬浮文字
        createInteractiveFloatingText(location, topName, TopType.SCORE);
        
        return true;
    }
    
    private void createInteractiveFloatingText(Location location, String topName, TopType topType) {
        // 获取排行榜数据
        List<Map<String, Object>> topPlayersData = getTopPlayersData(topType, 10);
        
        // 创建一个盔甲架作为标题
        World world = location.getWorld();
        if (world == null) {
            plugin.getLogger().warning("无法在指定世界中创建悬浮字，世界为空");
            return;
        }
        
        List<String> armorStandIds = new ArrayList<>();
        
        // 创建标题盔甲架
        Location titleLocation = location.clone().add(0, 3.0, 0); // 标题在上方
        ArmorStand titleStand = (ArmorStand) world.spawnEntity(titleLocation, EntityType.ARMOR_STAND);
        titleStand.setCustomName("§6=== " + topType.getDisplayName() + "排行榜 ===");
        titleStand.setCustomNameVisible(true);
        titleStand.setGravity(false);
        titleStand.setInvulnerable(true);
        titleStand.setVisible(false);
        armorStandIds.add(titleStand.getUniqueId().toString());
        
        // 创建提示信息盔甲架（说明如何切换排行榜）
        Location hintLocation = location.clone().add(0, 2.7, 0);
        ArmorStand hintStand = (ArmorStand) world.spawnEntity(hintLocation, EntityType.ARMOR_STAND);
        hintStand.setCustomName("§7点击切换排行榜类型");
        hintStand.setCustomNameVisible(true);
        hintStand.setGravity(false);
        hintStand.setInvulnerable(true);
        hintStand.setVisible(false);
        hintStand.setMarker(true); // 设置为标记，使其更容易被点击
        armorStandIds.add(hintStand.getUniqueId().toString());
        
        // 创建每个排名的盔甲架
        for (int i = 0; i < topPlayersData.size(); i++) {
            Map<String, Object> playerData = topPlayersData.get(i);
            String playerName = (String) playerData.get("name");
            Object value = playerData.get(getValueKey(topType));
            
            // 每个盔甲架位置稍微向下
            Location armorStandLocation = location.clone().add(0, 2.5 - (i * 0.3), 0);
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
            armorStand.setCustomName("§e" + (i + 1) + ". " + playerName + " - " + value);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setVisible(false); // 不显示盔甲架本身，只显示名字
            armorStandIds.add(armorStand.getUniqueId().toString());
        }
        
        // 保存盔甲架ID和当前类型到配置文件中
        String armorStandKey = "top-locations." + topName + ".armor-stand-ids";
        String currentTypeKey = "top-locations." + topName + ".current-type";
        plugin.getConfig().set(armorStandKey, armorStandIds);
        plugin.getConfig().set(currentTypeKey, topType.name());
        plugin.saveConfig();
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
    
    // 切换排行榜类型的方法
    public void switchTopType(String topName, Location location) {
        // 获取当前类型
        String currentTypeKey = "top-locations." + topName + ".current-type";
        String currentTypeName = plugin.getConfig().getString(currentTypeKey, "SCORE");
        
        TopType currentType;
        try {
            currentType = TopType.valueOf(currentTypeName);
        } catch (IllegalArgumentException e) {
            currentType = TopType.SCORE; // 默认为分数排行榜
        }
        
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
        
        // 删除现有的盔甲架
        removeFloatingText(topName);
        
        // 创建新的盔甲架
        createInteractiveFloatingText(location, topName, nextType);
    }
    
    // 提供一个公共方法来获取数据库管理器
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    private void removeFloatingText(String topName) {
        // 获取盔甲架ID列表
        String armorStandKey = "top-locations." + topName + ".armor-stand-ids";
        if (plugin.getConfig().contains(armorStandKey)) {
            List<String> armorStandIds = plugin.getConfig().getStringList(armorStandKey);
            
            // 删除每个盔甲架
            for (String uuidStr : armorStandIds) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    World world = plugin.getServer().getWorld(plugin.getConfig().getString("top-locations." + topName + ".world"));
                    if (world != null) {
                        for (org.bukkit.entity.Entity entity : world.getEntities()) {
                            if (entity.getUniqueId().equals(uuid) && entity instanceof ArmorStand) {
                                entity.remove();
                                break;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的UUID: " + uuidStr);
                }
            }
            
            // 清除配置中的盔甲架ID
            plugin.getConfig().set(armorStandKey, null);
        }
    }
}