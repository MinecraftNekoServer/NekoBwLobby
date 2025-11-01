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
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL;
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("数据库连接成功！");
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库连接失败: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getPlayerStats(String playerName) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
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
            }
            
            rs.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家数据失败: " + e.getMessage());
        }
        
        return stats;
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