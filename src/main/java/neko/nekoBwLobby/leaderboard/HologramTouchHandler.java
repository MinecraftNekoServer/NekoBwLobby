package neko.nekoBwLobby.leaderboard;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class HologramTouchHandler implements InvocationHandler {
    private final Leaderboard leaderboard;
    private final LeaderboardManager manager;

    public HologramTouchHandler(Leaderboard leaderboard, LeaderboardManager manager) {
        this.leaderboard = leaderboard;
        this.manager = manager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("onTouch".equals(method.getName()) && args.length == 2) {
            Player player = (Player) args[0];
            // 只有可切换的排行榜才处理点击
            if (leaderboard.isSwitchable()) {
                // 切换玩家的排行榜类型
                manager.switchLeaderboardType(leaderboard.getName(), player.getUniqueId());
                
                // 获取玩家当前的排行榜类型并显示消息
                LeaderboardType currentType = manager.getPlayerLeaderboardType(leaderboard.getName(), player.getUniqueId());
                player.sendMessage("§a已切换到: " + currentType.getDisplayName() + " 排行榜");
            }
        }
        return null;
    }
    
    public static Object createTouchHandler(Leaderboard leaderboard, LeaderboardManager manager) {
        try {
            Class<?> touchHandlerClass = Class.forName("com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler");
            return Proxy.newProxyInstance(
                HologramTouchHandler.class.getClassLoader(),
                new Class<?>[]{touchHandlerClass},
                new HologramTouchHandler(leaderboard, manager)
            );
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}