package neko.nekoBwLobby.listeners;

import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.commands.AddInteractiveTopCommand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class InteractiveTopClickListener implements Listener {

    private final NekoBwLobby plugin;

    public InteractiveTopClickListener(NekoBwLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // 检查是否点击了交互式排行榜的盔甲架
        if (isInteractiveTopArmorStand(armorStand)) {
            // 查找配置文件中保存的排行榜位置，找到对应的排行榜名称
            FileConfiguration config = plugin.getConfig();
            if (config.contains("top-locations")) {
                for (String topName : config.getConfigurationSection("top-locations").getKeys(false)) {
                    String type = config.getString("top-locations." + topName + ".type");
                    if ("interactive".equals(type)) {
                        // 检查盔甲架是否属于这个排行榜
                        if (isArmorStandOfTop(topName, armorStand)) {
                            // 获取位置信息
                            String worldName = config.getString("top-locations." + topName + ".world");
                            double x = config.getDouble("top-locations." + topName + ".x");
                            double y = config.getDouble("top-locations." + topName + ".y");
                            double z = config.getDouble("top-locations." + topName + ".z");
                            
                            World world = plugin.getServer().getWorld(worldName);
                            if (world != null) {
                                Location location = new Location(world, x, y, z);
                                
                                // 获取交互式排行榜命令实例并切换类型
                                AddInteractiveTopCommand command = new AddInteractiveTopCommand(plugin, plugin.getDatabaseManager());
                                command.switchTopType(topName, location);
                                
                                player.sendMessage("§a已切换排行榜类型！");
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isInteractiveTopArmorStand(ArmorStand armorStand) {
        // 检查这个盔甲架是否属于交互式排行榜
        // 可以通过自定义名称或其他标识来判断
        String customName = armorStand.getCustomName();
        if (customName != null) {
            return customName.contains("排行榜") || customName.contains("点击切换");
        }
        return false;
    }

    private boolean isArmorStandOfTop(String topName, ArmorStand clickedArmorStand) {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("top-locations." + topName + ".armor-stand-ids")) {
            java.util.List<String> armorStandIds = config.getStringList("top-locations." + topName + ".armor-stand-ids");
            return armorStandIds.contains(clickedArmorStand.getUniqueId().toString());
        }
        return false;
    }
}