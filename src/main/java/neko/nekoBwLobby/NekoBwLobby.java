package neko.nekoBwLobby;

import neko.nekoBwLobby.commands.GameInfoCommand;
import neko.nekoBwLobby.commands.AddTopCommand;
import neko.nekoBwLobby.commands.RemoveTopCommand;
import neko.nekoBwLobby.database.DatabaseManager;
import neko.nekoBwLobby.listeners.LeaderboardProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class NekoBwLobby extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override

    public void onEnable() {

        // Plugin startup logic

        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);


        // 检查数据库连接

        if (databaseManager.isConnectionValid()) {

            getLogger().info("数据库连接正常");

        } else {

            getLogger().severe("数据库连接异常，请检查配置文件");

        }

        // 注册指令
        getCommand("gameinfo").setExecutor(new GameInfoCommand(this, databaseManager));

        // 注册监听器

        getLogger().info("NekoBwLobby 插件已启用!");

    }




    @Override

    public void onDisable() {

        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("NekoBwLobby 插件已禁用!");
    }

    public DatabaseManager getDatabaseManager() {

        return databaseManager;

    }
}