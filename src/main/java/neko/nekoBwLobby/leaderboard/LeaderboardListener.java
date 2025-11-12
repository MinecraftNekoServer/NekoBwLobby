package neko.nekoBwLobby.leaderboard;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class LeaderboardListener implements Listener {
    private final LeaderboardManager manager;

    public LeaderboardListener(LeaderboardManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        // 检查盔甲架名称是否包含排行榜标识
        String customName = armorStand.getCustomName();
        if (customName != null && customName.contains("[LB:")) {
            // 提取排行榜名称
            int startIndex = customName.indexOf("[LB:") + 4; // "[LB:".length() = 4
            int endIndex = customName.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                String leaderboardName = customName.substring(startIndex, endIndex);
                
                // 检查该盔甲架是否属于指定的排行榜
                Leaderboard leaderboard = manager.getLeaderboard(leaderboardName);
                if (leaderboard != null && leaderboard.isPartOfLeaderboard(armorStand) && leaderboard.isSwitchable()) {
                    // 只有可切换的排行榜才响应点击
                    // 切换玩家的排行榜类型
                    manager.switchLeaderboardType(leaderboardName, player.getUniqueId());
                    
                    // 获取玩家当前的排行榜类型并显示消息
                    LeaderboardType currentType = manager.getPlayerLeaderboardType(leaderboardName, player.getUniqueId());
                    player.sendMessage("§a已切换到: " + currentType.getDisplayName() + " 排行榜");
                }
            }
        }
    }
}