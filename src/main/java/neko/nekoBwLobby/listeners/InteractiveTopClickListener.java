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
    private AddInteractiveTopCommand interactiveTopCommand;

    public InteractiveTopClickListener(NekoBwLobby plugin, AddInteractiveTopCommand command) {
        this.plugin = plugin;
        this.interactiveTopCommand = command;
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
            // 从盔甲架名称中提取排行榜名称
            String topName = extractTopName(armorStand);
            
            if (topName != null) {
                // 使用现有实例为当前玩家切换类型
                interactiveTopCommand.switchTopTypeForPlayer(topName, player);
            } else {
                player.sendMessage("§c错误：无法识别排行榜名称！"); // 错误消息
            }
        }
    }

    private boolean isInteractiveTopArmorStand(ArmorStand armorStand) {
        // 检查这个盔甲架是否属于交互式排行榜
        // 通过自定义名称中的特殊标识来判断
        String customName = armorStand.getCustomName();
        if (customName != null) {
            return customName.contains("[TOP:");
        }
        return false;
    }
    
    // 从盔甲架名称中提取topName
    private String extractTopName(ArmorStand armorStand) {
        String customName = armorStand.getCustomName();
        if (customName != null && customName.contains("[TOP:")) {
            int startIndex = customName.indexOf("[TOP:") + 5; // "[TOP:".length() = 5
            int endIndex = customName.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                return customName.substring(startIndex, endIndex);
            }
        }
        return null;
    }
}