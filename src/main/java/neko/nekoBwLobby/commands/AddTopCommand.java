package neko.nekoBwLobby.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.database.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

public class AddTopCommand implements CommandExecutor {
    
    private final NekoBwLobby plugin;
    private final DatabaseManager databaseManager;
    
    public AddTopCommand(NekoBwLobby plugin, DatabaseManager databaseManager) {
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
            player.sendMessage("§c用法: /addtop <name>");
            player.sendMessage("§c设置起床战争排行榜悬浮字的位置，显示前十名玩家");
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
        plugin.saveConfig();
        
        player.sendMessage("§a成功设置排行榜悬浮字位置: " + topName);
        player.sendMessage("§a位置: " + location.getWorld().getName() + ", " + 
                          String.format("%.2f, %.2f, %.2f", location.getX(), location.getY(), location.getZ()));
        
        // 获取排行榜数据并创建悬浮文字
        createFloatingText(location, topName);
        
        return true;
    }
    
    private void createFloatingText(Location location, String topName) {
        // 获取排行榜数据
        List<Map<String, Object>> topPlayersData = databaseManager.getTopPlayers(10);
        
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
        titleStand.setCustomName("§6=== 起床战争排行榜 ===");
        titleStand.setCustomNameVisible(true);
        titleStand.setGravity(false);
        titleStand.setInvulnerable(true);
        titleStand.setVisible(false);
        armorStandIds.add(titleStand.getUniqueId().toString());
        
        // 创建每个排名的盔甲架
        for (int i = 0; i < topPlayersData.size(); i++) {
            Map<String, Object> playerData = topPlayersData.get(i);
            String playerName = (String) playerData.get("name");
            Integer score = (Integer) playerData.get("score");
            
            // 每个盔甲架位置稍微向下
            Location armorStandLocation = location.clone().add(0, 2.5 - (i * 0.3), 0);
            ArmorStand armorStand = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
            armorStand.setCustomName("§e" + (i + 1) + ". " + playerName + " - " + score + "分");
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setVisible(false); // 不显示盔甲架本身，只显示名字
            armorStandIds.add(armorStand.getUniqueId().toString());
        }
        
        // 保存盔甲架ID到配置文件中，以便后续管理
        String armorStandKey = "top-locations." + topName + ".armor-stand-ids";
        plugin.getConfig().set(armorStandKey, armorStandIds);
        plugin.saveConfig();
    }
}