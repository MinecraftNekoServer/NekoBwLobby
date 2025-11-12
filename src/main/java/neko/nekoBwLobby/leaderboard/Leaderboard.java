package neko.nekoBwLobby.leaderboard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;

public class Leaderboard {
    private final String name;
    private final Location location;
    private final LeaderboardManager manager;
    private final LeaderboardType defaultType;
    private final boolean switchable;
    
    // 盔甲架实现
    private transient ArmorStand titleStand;
    private transient ArmorStand hintStand;
    private transient final ArmorStand[] rankStands = new ArmorStand[10];
    
    public Leaderboard(String name, Location location, LeaderboardManager manager, LeaderboardType defaultType, boolean switchable) {
        this.name = name;
        this.location = location;
        this.manager = manager;
        this.defaultType = defaultType;
        this.switchable = switchable;
    }
    
    public void spawn() {
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
            hintStand.setCustomName("§7点击切换排行榜类型");
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
            rankStands[i].setCustomName("§e" + (i + 1) + ". 加载中...");
            rankStands[i].setCustomNameVisible(true);
            rankStands[i].setGravity(false);
            rankStands[i].setInvulnerable(true);
            rankStands[i].setVisible(false);
            rankStands[i].setMarker(true);
        }
        
        // 初始更新数据
        updateForAllPlayers();
    }

    public void despawn() {
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
                    // 可切换排行榜不显示具体数值，只显示排名
                    if (switchable) {
                        rankStands[i].setCustomName("§e" + (i + 1) + ". " + playerName);
                    } else {
                        Object value = playerData.get(type.getConfigKey());
                        rankStands[i].setCustomName("§e" + (i + 1) + ". " + playerName + " - " + value);
                    }
                } else {
                    rankStands[i].setCustomName("§e" + (i + 1) + ". 无数据");
                }
            }
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
}