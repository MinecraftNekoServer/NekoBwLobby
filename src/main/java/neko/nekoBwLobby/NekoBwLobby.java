package neko.nekoBwLobby;

import neko.nekoBwLobby.commands.GameInfoCommand;
import neko.nekoBwLobby.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NekoBwLobby extends JavaPlugin {
    
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);
        
        // 注册指令
        getCommand("gameinfo").setExecutor(new GameInfoCommand(this, databaseManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
