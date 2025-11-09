package neko.nekoBwLobby.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.Bukkit;
import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.database.DatabaseManager;

import java.util.List;
import java.util.UUID;

public class RemoveTopCommand implements CommandExecutor {
    
    private final NekoBwLobby plugin;
    private final DatabaseManager databaseManager;
    
    public RemoveTopCommand(NekoBwLobby plugin, DatabaseManager databaseManager) {
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
            player.sendMessage("§c用法: /removetop <name>");
            player.sendMessage("§c删除指定名称的排行榜悬浮字");
            return true;
        }
        
        String topName = args[0];
        
        // 检查排行榜是否存在
        if (!plugin.getConfig().contains("top-locations." + topName)) {
            player.sendMessage("§c未找到名称为 " + topName + " 的排行榜");
            return true;
        }
        
        // 删除盔甲架
        removeFloatingText(topName);
        
        // 从配置文件中删除
        plugin.getConfig().set("top-locations." + topName, null);
        plugin.saveConfig();
        
        player.sendMessage("§a成功删除排行榜悬浮字: " + topName);
        
        return true;
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
                    // 在所有世界中查找该实体
                    for (World world : Bukkit.getWorlds()) {
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