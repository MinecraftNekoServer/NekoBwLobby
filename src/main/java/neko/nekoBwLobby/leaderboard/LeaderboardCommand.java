package neko.nekoBwLobby.leaderboard;

import neko.nekoBwLobby.NekoBwLobby;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderboardCommand implements CommandExecutor {
    private final LeaderboardManager manager;
    private final NekoBwLobby plugin;

    public LeaderboardCommand(NekoBwLobby plugin, LeaderboardManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此指令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§c用法: /addinteractivetop <name> [type]");
            player.sendMessage("§c创建一个新的排行榜");
            player.sendMessage("§c参数: name - 排行榜名称, type - 类型 (fixed/score/switchable)");
            return true;
        }

        String name = args[0];
        String type = args.length > 1 ? args[1] : "switchable";
        
        LeaderboardType leaderboardType;
        boolean isSwitchable = true;
        
        switch (type.toLowerCase()) {
            case "fixed":
            case "score":
                leaderboardType = LeaderboardType.SCORE;
                isSwitchable = false;
                break;
            case "switchable":
            default:
                leaderboardType = LeaderboardType.SCORE;
                isSwitchable = true;
                break;
        }
        
        manager.createLeaderboard(name, player.getLocation(), leaderboardType, isSwitchable);
        player.sendMessage("§a成功创建排行榜: " + name + " (" + type + ")");
        player.sendMessage("§a位置: " + player.getLocation().getWorld().getName() + ", " +
                String.format("%.2f, %.2f, %.2f", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

        return true;
    }
}