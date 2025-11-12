package neko.nekoBwLobby.leaderboard;

import neko.nekoBwLobby.NekoBwLobby;
import neko.nekoBwLobby.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {
    
    private final NekoBwLobby plugin;

    private final DatabaseManager databaseManager;

    private LeaderboardListener listener;

    

    // 存储所有排行榜

    private final Map<String, Leaderboard> leaderboards = new HashMap<>();

    

    // 存储每个玩家的排行榜偏好

    private final Map<UUID, Map<String, LeaderboardType>> playerPreferences = new ConcurrentHashMap<>();
    
    public LeaderboardManager(NekoBwLobby plugin, DatabaseManager databaseManager) {

        this.plugin = plugin;

        this.databaseManager = databaseManager;

        this.listener = new LeaderboardListener(this); // 初始化listener

        

        // 启动自动刷新任务，每10秒刷新一次

        startAutoRefreshTask();

    }

    

    private void startAutoRefreshTask() {

        new BukkitRunnable() {

            @Override

            public void run() {

                for (Leaderboard board : leaderboards.values()) {

                    board.updateForAllPlayers();

                }

            }

        }.runTaskTimerAsynchronously(plugin, 200L, 200L); // 200 ticks = 10秒

    }
    
    public void createLeaderboard(String name, Location location, LeaderboardType type, boolean isSwitchable) {
        Leaderboard leaderboard = new Leaderboard(name, location, this, type, isSwitchable);
        leaderboards.put(name, leaderboard);
        leaderboard.spawn();
        saveToConfig(); // 保存到配置文件
    }
    
    public void removeLeaderboard(String name) {
        Leaderboard leaderboard = leaderboards.get(name);
        if (leaderboard != null) {
            leaderboard.despawn();
            leaderboards.remove(name);
            // 从配置文件中移除
            plugin.getConfig().set("leaderboards.boards." + name, null);
            plugin.saveConfig();
        }
    }
    
    public Leaderboard getLeaderboard(String name) {
        return leaderboards.get(name);
    }
    
    public void switchLeaderboardType(String name, UUID playerUUID) {
        Leaderboard leaderboard = leaderboards.get(name);
        if (leaderboard != null && leaderboard.isSwitchable()) {
            // 获取玩家当前的排行榜类型
            Map<String, LeaderboardType> playerLeaderboards = playerPreferences.computeIfAbsent(playerUUID, k -> new HashMap<>());
            LeaderboardType currentType = playerLeaderboards.getOrDefault(name, leaderboard.getDefaultType());
            LeaderboardType nextType = getNextNonScoreType(currentType);
            
            // 更新玩家的偏好
            playerLeaderboards.put(name, nextType);
            
            // 更新排行榜显示
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                leaderboard.updateForPlayer(player, nextType);
            }
        }
    }
    
    // 获取下一个非分数类型（用于可切换排行榜）
    private LeaderboardType getNextNonScoreType(LeaderboardType currentType) {
        if (currentType == LeaderboardType.SCORE) {
            return LeaderboardType.KILLS;
        }
        
        LeaderboardType[] values = {LeaderboardType.KILLS, LeaderboardType.DESTROYED_BEDS, LeaderboardType.DEATHS};
        int currentIndex = Arrays.asList(values).indexOf(currentType);
        if (currentIndex == -1) {
            return LeaderboardType.KILLS;
        }
        int nextIndex = (currentIndex + 1) % values.length;
        return values[nextIndex];
    }
    
    public LeaderboardType getPlayerLeaderboardType(String name, UUID playerUUID) {
        Leaderboard leaderboard = leaderboards.get(name);
        if (leaderboard != null) {
            if (!leaderboard.isSwitchable()) {
                return leaderboard.getDefaultType();
            }
            return playerPreferences
                    .computeIfAbsent(playerUUID, k -> new HashMap<>())
                    .getOrDefault(name, leaderboard.getDefaultType());
        }
        return LeaderboardType.SCORE;
    }
    
    public Map<String, Leaderboard> getLeaderboards() {

        return leaderboards;

    }

    

    public DatabaseManager getDatabaseManager() {

        return databaseManager;

    }

    

    public LeaderboardListener getListener() {

        return listener;

    }
    
    public void saveToConfig() {
        // 保存排行榜位置到配置文件
        for (Leaderboard board : leaderboards.values()) {
            String path = "leaderboards.boards." + board.getName();
            Location loc = board.getLocation();
            plugin.getConfig().set(path + ".world", loc.getWorld().getName());
            plugin.getConfig().set(path + ".x", loc.getX());
            plugin.getConfig().set(path + ".y", loc.getY());
            plugin.getConfig().set(path + ".z", loc.getZ());
            plugin.getConfig().set(path + ".type", board.getDefaultType().name());
            plugin.getConfig().set(path + ".switchable", board.isSwitchable());
        }
        plugin.saveConfig();
    }
    
    public void loadFromConfig() {
        // 从配置文件加载排行榜
        if (plugin.getConfig().contains("leaderboards.boards")) {
            for (String name : plugin.getConfig().getConfigurationSection("leaderboards.boards").getKeys(false)) {
                String path = "leaderboards.boards." + name;
                String worldName = plugin.getConfig().getString(path + ".world");
                double x = plugin.getConfig().getDouble(path + ".x");
                double y = plugin.getConfig().getDouble(path + ".y");
                double z = plugin.getConfig().getDouble(path + ".z");
                String typeStr = plugin.getConfig().getString(path + ".type", "SCORE");
                boolean switchable = plugin.getConfig().getBoolean(path + ".switchable", true);
                
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    LeaderboardType type = LeaderboardType.valueOf(typeStr);
                    Leaderboard leaderboard = new Leaderboard(name, location, this, type, switchable);
                    leaderboards.put(name, leaderboard);
                    leaderboard.spawn();
                }
            }
        }
    }
}