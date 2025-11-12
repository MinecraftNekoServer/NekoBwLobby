package neko.nekoBwLobby.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;
import java.util.*;

public class Leaderboard {
    private final String name;
    private final Location location;
    private final LeaderboardManager manager;
    private final LeaderboardType defaultType;
    private final boolean switchable;
    
    // 排行榜显示类型
    private DisplayType displayType = DisplayType.ARMOR_STAND;
    
    // 盔甲架实现
    private transient ArmorStand titleStand;
    private transient ArmorStand hintStand;
    private transient final ArmorStand[] rankStands = new ArmorStand[10];
    
    // HolographicDisplays实现
    private transient Object hologram; // Hologram对象
    private transient List<Object> lines = new ArrayList<>(); // TextLine对象列表
    
    public enum DisplayType {
        ARMOR_STAND,
        HOLOGRAPHIC_DISPLAYS,
        HOLOGRAM_API
    }
    
    public Leaderboard(String name, Location location, LeaderboardManager manager, LeaderboardType defaultType, boolean switchable) {
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.defaultType = defaultType;
        this.switchable = switchable;
        
        // 尝试检测HolographicDisplays插件是否存在
        if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            displayType = DisplayType.HOLOGRAPHIC_DISPLAYS;
        } else if (Bukkit.getPluginManager().getPlugin("HologramAPI") != null) {
            displayType = DisplayType.HOLOGRAM_API;
        }
    }
    
    public void spawn() {
        switch (displayType) {
            case HOLOGRAPHIC_DISPLAYS:
                spawnHolographicDisplays();
                break;
            case HOLOGRAM_API:
                spawnHologramAPI();
                break;
            default:
                spawnArmorStands();
                break;
        }
        
        // 初始更新数据
        updateForAllPlayers();
    }
    
    private void spawnArmorStands() {
        World world = location.getWorld();
        if (world == null) return;
        
        // 创建标题盔甲架
        Location titleLocation = location.clone().add(0, 3.0, 0);
        titleStand = (ArmorStand) world.spawnEntity(titleLocation, EntityType.ARMOR_STAND);
        titleStand.setCustomName("§6=== " + defaultType.getDisplayName() + "排行榜 ===");
        titleStand.setCustomNameVisible(true);
        titleStand.setGravity(false);
        titleStand.setInvulnerable(true);
        titleStand.setVisible(false);
        titleStand.setMarker(true);
        
        // 创建提示信息盔甲架（仅对可切换排行榜显示）
        if (switchable) {
            Location hintLocation = location.clone().add(0, 2.7, 0);
            hintStand = (ArmorStand) world.spawnEntity(hintLocation, EntityType.ARMOR_STAND);
            hintStand.setCustomName("§7点击切换排行榜类型 [LB:" + name + "]");
            hintStand.setCustomNameVisible(true);
            hintStand.setGravity(false);
            hintStand.setInvulnerable(true);
            hintStand.setVisible(false);
            hintStand.setMarker(true);
        }
        
        // 创建排名盔甲架
        for (int i = 0; i < 10; i++) {
            Location rankLocation = location.clone().add(0, 2.5 - (i * 0.3), 0);
            rankStands[i] = (ArmorStand) world.spawnEntity(rankLocation, EntityType.ARMOR_STAND);
            rankStands[i].setCustomName("§e" + (i + 1) + ". 加载中... [LB:" + name + "]");
            rankStands[i].setCustomNameVisible(true);
            rankStands[i].setGravity(false);
            rankStands[i].setInvulnerable(true);
            rankStands[i].setVisible(false);
            rankStands[i].setMarker(true);
        }
    }
    
    private void spawnHolographicDisplays() {
        try {
            // 使用反射调用HolographicDisplays API
            Class<?> hologramsAPIClass = Class.forName("com.gmail.filoghost.holographicdisplays.api.HologramsAPI");
            Class<?> bukkitWorldClass = Class.forName("org.bukkit.World");
            Method createHologramMethod = hologramsAPIClass.getMethod("createHologram", bukkitWorldClass, Location.class);
            
            World world = location.getWorld();
            if (world == null) return;
            
            Location holoLocation = location.clone().add(0, 3.0, 0);
            hologram = createHologramMethod.invoke(null, world, holoLocation);
            
            // 添加标题
            Method appendTextLineMethod = hologram.getClass().getMethod("appendTextLine", String.class);
            Object titleLine = appendTextLineMethod.invoke(hologram, "§6=== " + defaultType.getDisplayName() + "排行榜 ===");
            lines.add(titleLine);
            
            // 如果是可切换的排行榜，添加提示
            if (switchable) {
                Object hintLine = appendTextLineMethod.invoke(hologram, "§7点击切换排行榜类型 [LB:" + name + "]");
                lines.add(hintLine);
            }
            
            // 添加排名行
            for (int i = 0; i < 10; i++) {
                Object rankLine = appendTextLineMethod.invoke(hologram, "§e" + (i + 1) + ". 加载中... [LB:" + name + "]");
                lines.add(rankLine);
            }
        } catch (Exception e) {
            // 如果HolographicDisplays不可用，回退到盔甲架
            displayType = DisplayType.ARMOR_STAND;
            spawnArmorStands();
        }
    }
    
    private void spawnHologramAPI() {
        try {
            // 使用反射调用HologramAPI
            Class<?> hologramAPIClass = Class.forName("de.inventivegames.hologram.Hologram");
            Class<?> locationClass = Class.forName("org.bukkit.Location");
            
            // 创建全息图
            World world = location.getWorld();
            if (world == null) return;
            
            Location holoLocation = location.clone().add(0, 3.0, 0);
            hologram = hologramAPIClass.getConstructor(locationClass).newInstance(holoLocation);
            
            // 添加标题
            Method addLineMethod = hologram.getClass().getMethod("addLine", String.class);
            addLineMethod.invoke(hologram, "§6=== " + defaultType.getDisplayName() + "排行榜 ===");
            lines.add(hologram); // 使用hologram对象本身作为占位符
            
            // 如果是可切换的排行榜，添加提示
            if (switchable) {
                addLineMethod.invoke(hologram, "§7点击切换排行榜类型 [LB:" + name + "]");
                lines.add(hologram); // 使用hologram对象本身作为占位符
            }
            
            // 添加排名行
            for (int i = 0; i < 10; i++) {
                addLineMethod.invoke(hologram, "§e" + (i + 1) + ". 加载中... [LB:" + name + "]");
                lines.add(hologram); // 使用hologram对象本身作为占位符
            }
            
            // 显示全息图
            Method showMethod = hologram.getClass().getMethod("show");
            showMethod.invoke(hologram);
        } catch (Exception e) {
            // 如果HologramAPI不可用，回退到盔甲架
            displayType = DisplayType.ARMOR_STAND;
            spawnArmorStands();
        }
    }

    public void despawn() {
        switch (displayType) {
            case HOLOGRAPHIC_DISPLAYS:
            case HOLOGRAM_API:
                despawnHologram();
                break;
            default:
                despawnArmorStands();
                break;
        }
    }
    
    private void despawnArmorStands() {
        // 移除所有盔甲架
        if (titleStand != null && titleStand.isValid()) {
            titleStand.remove();
        }
        if (hintStand != null && hintStand.isValid()) {
            hintStand.remove();
        }
        for (ArmorStand stand : rankStands) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
    }
    
    private void despawnHologram() {
        try {
            if (hologram != null) {
                if (displayType == DisplayType.HOLOGRAPHIC_DISPLAYS) {
                    Method deleteMethod = hologram.getClass().getMethod("delete");
                    deleteMethod.invoke(hologram);
                } else if (displayType == DisplayType.HOLOGRAM_API) {
                    Method destroyMethod = hologram.getClass().getMethod("destroy");
                    destroyMethod.invoke(hologram);
                }
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
        switch (displayType) {
            case HOLOGRAPHIC_DISPLAYS:
                updateHolographicDisplays(type);
                break;
            case HOLOGRAM_API:
                updateHologramAPI(type);
                break;
            default:
                updateArmorStands(type);
                break;
        }
    }
    
    private void updateArmorStands(LeaderboardType type) {
        if (titleStand == null || !titleStand.isValid()) return;
        
        // 获取最新的排行榜数据
        List<Map<String, Object>> topPlayers = getTopPlayersData(type);
        
        // 更新标题
        titleStand.setCustomName("§6=== " + type.getDisplayName() + "排行榜 ===");
        
        // 更新排名
        for (int i = 0; i < 10; i++) {
            if (rankStands[i] != null && rankStands[i].isValid()) {
                if (i < topPlayers.size()) {
                    Map<String, Object> playerData = topPlayers.get(i);
                    String playerName = (String) playerData.get("name");
                    Object value = playerData.get(type.getConfigKey());
                    rankStands[i].setCustomName("§e" + (i + 1) + ". " + playerName + " - " + value + " [LB:" + name + "]");
                } else {
                    rankStands[i].setCustomName("§e" + (i + 1) + ". 无数据 [LB:" + name + "]");
                }
            }
        }
    }
    
    private void updateHolographicDisplays(LeaderboardType type) {
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
                        Object value = playerData.get(type.getConfigKey());
                        Object line = lines.get(lineIndex);
                        Method setTextMethod = line.getClass().getMethod("setText", String.class);
                        setTextMethod.invoke(line, "§e" + (i + 1) + ". " + playerName + " - " + value + " [LB:" + name + "]");
                    } else {
                        Object line = lines.get(lineIndex);
                        Method setTextMethod = line.getClass().getMethod("setText", String.class);
                        setTextMethod.invoke(line, "§e" + (i + 1) + ". 无数据 [LB:" + name + "]");
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
    
    private void updateHologramAPI(LeaderboardType type) {
        try {
            if (hologram == null) return;
            
            // 获取最新的排行榜数据
            List<Map<String, Object>> topPlayers = getTopPlayersData(type);
            
            // 重建全息图行（HologramAPI通常需要重新创建）
            Method clearLinesMethod = hologram.getClass().getMethod("clearLines");
            clearLinesMethod.invoke(hologram);
            
            Method addLineMethod = hologram.getClass().getMethod("addLine", String.class);
            
            // 添加标题
            addLineMethod.invoke(hologram, "§6=== " + type.getDisplayName() + "排行榜 ===");
            
            // 如果是可切换的排行榜，添加提示
            if (switchable) {
                addLineMethod.invoke(hologram, "§7点击切换排行榜类型 [LB:" + name + "]");
            }
            
            // 添加排名行
            for (int i = 0; i < 10; i++) {
                if (i < topPlayers.size()) {
                    Map<String, Object> playerData = topPlayers.get(i);
                    String playerName = (String) playerData.get("name");
                    Object value = playerData.get(type.getConfigKey());
                    addLineMethod.invoke(hologram, "§e" + (i + 1) + ". " + playerName + " - " + value + " [LB:" + name + "]");
                } else {
                    addLineMethod.invoke(hologram, "§e" + (i + 1) + ". 无数据 [LB:" + name + "]");
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
    
    public boolean isPartOfLeaderboard(ArmorStand armorStand) {
        if (displayType != DisplayType.ARMOR_STAND) return false;
        
        if (titleStand != null && titleStand.equals(armorStand)) return true;
        if (hintStand != null && hintStand.equals(armorStand)) return true;
        for (ArmorStand stand : rankStands) {
            if (stand != null && stand.equals(armorStand)) return true;
        }
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
    
    public DisplayType getDisplayType() {
        return displayType;
    }
}