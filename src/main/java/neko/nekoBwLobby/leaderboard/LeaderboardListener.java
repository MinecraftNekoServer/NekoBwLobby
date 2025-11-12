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

        // 遍历所有排行榜，检查是否点击了提示行
        for (String leaderboardName : manager.getLeaderboards().keySet()) {
            Leaderboard leaderboard = manager.getLeaderboard(leaderboardName);
            if (leaderboard != null && leaderboard.isPartOfLeaderboard(armorStand) && leaderboard.isSwitchable()) {
                // 切换玩家的排行榜类型
                manager.switchLeaderboardType(leaderboardName, player.getUniqueId());
                
                // 获取玩家当前的排行榜类型并显示消息
                LeaderboardType currentType = manager.getPlayerLeaderboardType(leaderboardName, player.getUniqueId());
                player.sendMessage("§a已切换到: " + currentType.getDisplayName() + " 排行榜");
                
                break;
            }
        }
    }
}