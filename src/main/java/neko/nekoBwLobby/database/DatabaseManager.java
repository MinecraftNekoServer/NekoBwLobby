package neko.nekoBwLobby.database;

import neko.nekoBwLobby.NekoBwLobby;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private final NekoBwLobby plugin;
    private Connection connection;
    
    public DatabaseManager(NekoBwLobby plugin) {
        this.plugin = plugin;
        connect();
    }
    
    private void connect() {
        try {
            FileConfiguration config = plugin.getConfig();
            String host = config.getString("database.host");
            int port = config.getInt("database.port");
            String database = config.getString("database.name");
            String username = config.getString("database.username");
            String password = config.getString("database.password");
            boolean useSSL = config.getBoolean("database.useSSL");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&useUnicode=true&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("数据库连接成功！");
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 直接从数据库获取玩家统计信息，不使用任何缓存
     * @param playerName 玩家名称
     * @return 玩家统计数据
     */
    public Map<String, Object> getPlayerStats(String playerName) {
        Map<String, Object> stats = new HashMap<>();
        
        // 如果连接断开，尝试重新连接
        try {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().warning("数据库连接已断开，正在尝试重新连接...");
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("检查数据库连接状态时出错: " + e.getMessage());
        }
        
        try {
            // 记录查询日志用于调试
            plugin.getLogger().info("正在查询玩家 '" + playerName + "' 的数据...");
            
            PreparedStatement statement = connection.prepareStatement(
                "SELECT kills, wins, score, loses, destroyedBeds, deaths FROM bw_stats_players WHERE name = ?"
            );
            statement.setString(1, playerName);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                stats.put("kills", rs.getInt("kills"));
                stats.put("wins", rs.getInt("wins"));
                stats.put("score", rs.getInt("score"));
                stats.put("loses", rs.getInt("loses"));
                stats.put("destroyedBeds", rs.getInt("destroyedBeds"));
                stats.put("deaths", rs.getInt("deaths"));
                
                plugin.getLogger().info("成功获取到玩家 '" + playerName + "' 的数据: " +
                    "kills=" + stats.get("kills") + ", " +
                    "wins=" + stats.get("wins") + ", " +
                    "score=" + stats.get("score") + ", " +
                    "loses=" + stats.get("loses") + ", " +
                    "destroyedBeds=" + stats.get("destroyedBeds") + ", " +
                    "deaths=" + stats.get("deaths"));
            } else {
                plugin.getLogger().warning("在数据库中未找到玩家 '" + playerName + "' 的数据");
            }
            
            rs.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家数据失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息
        }
        
        return stats;
    }
    
    /**
     * 测试数据库连接是否正常
     * @return 连接是否正常
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("关闭数据库连接时出错: " + e.getMessage());
        }
    }
}