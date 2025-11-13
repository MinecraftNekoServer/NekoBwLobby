package neko.nekoBwLobby;

import neko.nekoBwLobby.commands.GameInfoCommand;
import neko.nekoBwLobby.commands.AddTopCommand;
import neko.nekoBwLobby.commands.RemoveTopCommand;
import neko.nekoBwLobby.database.DatabaseManager;
import neko.nekoBwLobby.leaderboard.LeaderboardManager;
import neko.nekoBwLobby.leaderboard.LeaderboardCommand;
import neko.nekoBwLobby.leaderboard.LeaderboardListener;
import neko.nekoBwLobby.listeners.LeaderboardProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class NekoBwLobby extends JavaPlugin {

    private DatabaseManager databaseManager;
    private LeaderboardManager leaderboardManager;

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

        // 初始化排行榜管理器
        leaderboardManager = new LeaderboardManager(this, databaseManager);
        leaderboardManager.loadFromConfig();

        // 注册指令
        getCommand("gameinfo").setExecutor(new GameInfoCommand(this, databaseManager));
        getCommand("addtop").setExecutor(new AddTopCommand(this, databaseManager));
        getCommand("removetop").setExecutor(new RemoveTopCommand(this, databaseManager));
        getCommand("addinteractivetop").setExecutor(new LeaderboardCommand(this, leaderboardManager));

        // 注册监听器
        Bukkit.getPluginManager().registerEvents(new LeaderboardListener(leaderboardManager), this);
        Bukkit.getPluginManager().registerEvents(new LeaderboardProtectionListener(leaderboardManager), this);

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

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}