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

        // 由于现在只使用HolographicDisplays，盔甲架不再属于排行榜
        // 此事件处理器不再需要处理排行榜切换
    }
}