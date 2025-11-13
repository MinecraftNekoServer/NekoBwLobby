package neko.nekoBwLobby.leaderboard;

import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import neko.nekoBwLobby.hologram.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;

public class Leaderboard {
    private final String name;
    private final Location location;
    private final LeaderboardManager manager;
    private final LeaderboardType defaultType;
    private final boolean switchable;
    
    // HolographicDisplays实现
    private transient Object hologram; // Hologram对象
    private transient List<Object> lines = new ArrayList<>(); // TextLine对象列表
    
    public Leaderboard(String name, Location location, LeaderboardManager manager, LeaderboardType defaultType, boolean switchable) {
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.defaultType = defaultType;
        this.switchable = switchable;
    }
    
    public void spawn() {
        spawnHolographicDisplays();
        // 初始更新数据
        updateForAllPlayers();
    }
    
    private void spawnHolographicDisplays() {
        try {
            // 检查HolographicDisplays插件是否存在
            if (!HologramManager.isHolographicDisplaysEnabled()) {
                Bukkit.getLogger().severe("错误：未安装HolographicDisplays插件，无法创建排行榜！");
                return;
            }
            
            World world = location.getWorld();
            if (world == null) return;
            
            Location holoLocation = location.clone().add(0, 3.0, 0);
            
            // 使用HologramManager创建全息对象
            Object hologramObj = HologramManager.createHologram(manager.getPlugin(), holoLocation);
            
            // 保存全息对象引用
            if (hologramObj != null) {
                hologram = hologramObj;
                
                // 使用HologramManager添加文本行
                // 添加标题
                Object titleLine = HologramManager.appendTextLine(hologramObj, "§6=== " + defaultType.getDisplayName() + "排行榜 ===");
                if (titleLine != null) {
                    lines.add(titleLine);
                }
                
                // 如果是可切换的排行榜，添加提示
                if (switchable) {
                    Object hintLine = HologramManager.appendTextLine(hologramObj, "§7点击切换排行榜类型");
                    if (hintLine != null) {
                        lines.add(hintLine);
                    }
                    
                    // 注册触摸处理器
                    try {
                        Object touchHandler = HologramTouchHandler.createTouchHandler(this, manager);
                        if (touchHandler != null) {
                            HologramManager.setTouchHandler(hologramObj, touchHandler);
                        }
                    } catch (Exception e) {
                        // 触摸处理器注册失败不影响主要功能
                        Bukkit.getLogger().warning("无法注册排行榜触摸处理器: " + e.getMessage());
                    }
                }
                
                // 添加排名行
                for (int i = 0; i < 10; i++) {
                    Object rankLine = HologramManager.appendTextLine(hologramObj, "§e" + (i + 1) + ". 加载中...");
                    if (rankLine != null) {
                        lines.add(rankLine);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("创建排行榜时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void despawn() {
        despawnHologram();
    }
    
    private void despawnHologram() {
        try {
            if (hologram != null) {
                HologramManager.deleteHologram(hologram);
                hologram = null;
                lines.clear();
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    public void updateForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateForPlayer(player);
        }
    }
    
    public void updateForPlayer(Player player) {
        LeaderboardType type = manager.getPlayerLeaderboardType(name, player.getUniqueId());
        updateForPlayer(player, type);
    }
    
    public void updateForPlayer(Player player, LeaderboardType type) {
        updateHologram(type);
    }
    
    private void updateHologram(LeaderboardType type) {
        try {
            if (hologram == null) return;
            
            // 获取最新的排行榜数据
            List<Map<String, Object>> topPlayers = getTopPlayersData(type);
            
            // 更新标题
            if (!lines.isEmpty()) {
                Object titleLine = lines.get(0);
                Method setTextMethod = titleLine.getClass().getMethod("setText", String.class);
                setTextMethod.invoke(titleLine, "§6=== " + type.getDisplayName() + "排行榜 ===");
            }
            
            // 更新排名（跳过标题和可能的提示行）
            int startIndex = switchable ? 2 : 1; // 如果有提示行，从索引2开始；否则从索引1开始
            for (int i = 0; i < 10; i++) {
                int lineIndex = startIndex + i;
                if (lineIndex < lines.size()) {
                    if (i < topPlayers.size()) {
                        Map<String, Object> playerData = topPlayers.get(i);
                        String playerName = (String) playerData.get("name");
                        // 可切换排行榜不显示具体数值，只显示排名
                        if (switchable) {
                            Object line = lines.get(lineIndex);
                            Method setTextMethod = line.getClass().getMethod("setText", String.class);
                            setTextMethod.invoke(line, "§e" + (i + 1) + ". " + playerName);
                        } else {
                            Object value = playerData.get(type.getConfigKey());
                            Object line = lines.get(lineIndex);
                            Method setTextMethod = line.getClass().getMethod("setText", String.class);
                            setTextMethod.invoke(line, "§e" + (i + 1) + ". " + playerName + " - " + value);
                        }
                    } else {
                        Object line = lines.get(lineIndex);
                        Method setTextMethod = line.getClass().getMethod("setText", String.class);
                        setTextMethod.invoke(line, "§e" + (i + 1) + ". 无数据");
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    private List<Map<String, Object>> getTopPlayersData(LeaderboardType type) {
        switch (type) {
            case KILLS:
                return manager.getDatabaseManager().getTopKills(10);
            case DESTROYED_BEDS:
                return manager.getDatabaseManager().getTopDestroyedBeds(10);
            case DEATHS:
                return manager.getDatabaseManager().getTopDeaths(10);
            case SCORE:
            default:
                return manager.getDatabaseManager().getTopPlayers(10);
        }
    }
    
    public boolean isPartOfLeaderboard(org.bukkit.entity.ArmorStand armorStand) {
        // 不再使用盔甲架，直接返回false
        return false;
    }
    
    public boolean isSwitchable() {
        return switchable;
    }
    
    public LeaderboardType getDefaultType() {
        return defaultType;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public Object getHologram() {
        return hologram;
    }
}