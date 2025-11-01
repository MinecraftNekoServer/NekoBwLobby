package neko.nekoBwLobby.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.database.DatabaseManager;
import neko.nekoBwLobby.gui.GameInfoGUI;

public class GameInfoCommand implements CommandExecutor {
    
    private final NekoBwLobby plugin;
    private final DatabaseManager databaseManager;
    
    public GameInfoCommand(NekoBwLobby plugin, DatabaseManager databaseManager) {
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
        
        // 打开GUI显示玩家游戏数据
        GameInfoGUI.openGUI(player, databaseManager);
        
        return true;
    }
}