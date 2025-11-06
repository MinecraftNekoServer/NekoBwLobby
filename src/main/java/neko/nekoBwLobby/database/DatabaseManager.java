package neko.nekoBwLobby.database;

import neko.nekoBwLobby.NekoBwLobby;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private final NekoBwLobby plugin;
    
    public DatabaseManager(NekoBwLobby plugin) {
        this.plugin = plugin;
        // 不再在构造函数中自动连接数据库
    }
    
    /**
     * 创建临时数据库连接
     * @return 数据库连接对象
     * @throws SQLException SQL异常
     */
    private Connection createConnection() throws SQLException {
        return createConnectionWithRetry(3); // 默认重试3次
    }
    
    /**
     * 创建临时数据库连接（带重试机制）
     * @param maxRetries 最大重试次数
     * @return 数据库连接对象
     * @throws SQLException SQL异常
     */
    private Connection createConnectionWithRetry(int maxRetries) throws SQLException {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.name");
        String username = config.getString("database.username");
        String password = config.getString("database.password");
        boolean useSSL = config.getBoolean("database.useSSL");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL + "&useUnicode=true&characterEncoding=UTF-8";
        
        SQLException lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                plugin.getLogger().info("尝试连接数据库... (第" + (i + 1) + "次)");
                Connection connection = DriverManager.getConnection(url, username, password);
                plugin.getLogger().info("数据库连接成功！");
                return connection;
            } catch (SQLException e) {
                lastException = e;
                plugin.getLogger().warning("数据库连接失败: " + e.getMessage() + " (第" + (i + 1) + "次)");
                if (i < maxRetries - 1) {
                    // 等待一段时间再重试
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("连接中断", ie);
                    }
                }
            }
        }
        
        throw new SQLException("无法连接到数据库，已重试" + maxRetries + "次", lastException);
    }
    
    /**
     * 直接从数据库获取玩家统计信息，不使用任何缓存
     * @param playerName 玩家名称
     * @return 玩家统计数据
     */
    public Map<String, Object> getPlayerStats(String playerName) {
        Map<String, Object> stats = new HashMap<>();
        Connection tempConnection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try {
            // 创建临时连接（带重试机制）
            tempConnection = createConnectionWithRetry(3);
            
            // 记录查询日志用于调试
            plugin.getLogger().info("正在查询玩家 '" + playerName + "' 的数据...");
            
            statement = tempConnection.prepareStatement(
                "SELECT kills, wins, score, loses, destroyedBeds, deaths FROM bw_stats_players WHERE name = ?"
            );
            statement.setString(1, playerName);
            rs = statement.executeQuery();
            
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
        } catch (SQLException e) {
            plugin.getLogger().severe("获取玩家数据失败: " + e.getMessage());
            e.printStackTrace(); // 打印详细错误信息
        } finally {
            // 关闭所有资源
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (tempConnection != null) tempConnection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("关闭数据库资源时出错: " + e.getMessage());
            }
        }
        
        return stats;
    }
    
    /**
     * 测试数据库连接是否正常
     * @return 连接是否正常
     */
    public boolean isConnectionValid() {
        Connection tempConnection = null;
        try {
            tempConnection = createConnectionWithRetry(2); // 测试连接时重试2次
            return tempConnection != null && !tempConnection.isClosed() && tempConnection.isValid(5);
        } catch (SQLException e) {
            plugin.getLogger().severe("数据库连接测试失败: " + e.getMessage());
            return false;
        } finally {
            // 关闭测试连接
            try {
                if (tempConnection != null && !tempConnection.isClosed()) {
                    tempConnection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("关闭测试数据库连接时出错: " + e.getMessage());
            }
        }
    }
    
    public void close() {
        // 不再需要关闭持久连接，因为使用的是临时连接
        plugin.getLogger().info("数据库管理器已关闭");
    }
}