package neko.nekoBwLobby.listeners;

import neko.nekoBwLobby.leaderboard.LeaderboardManager;
import neko.nekoBwLobby.NekoBwLobby;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class LeaderboardProtectionListener implements Listener {
    
    private final LeaderboardManager leaderboardManager;
    
    public LeaderboardProtectionListener(LeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // 检查是否是排行榜相关的盔甲架
        if (entity.getType() == EntityType.ARMOR_STAND) {
            ArmorStand armorStand = (ArmorStand) entity;
            
            // 由于现在只使用HolographicDisplays，盔甲架不再属于排行榜
            // 此事件处理器不再需要处理排行榜保护
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        
        // 检查是否是排行榜相关的盔甲架
        if (entity.getType() == EntityType.ARMOR_STAND) {
            ArmorStand armorStand = (ArmorStand) entity;
            
            // 由于现在只使用HolographicDisplays，盔甲架不再属于排行榜
            // 此事件处理器不再需要处理排行榜保护
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // 检查是否是排行榜相关的盔甲架
        if (entity.getType() == EntityType.ARMOR_STAND) {
            ArmorStand armorStand = (ArmorStand) entity;
            
            // 由于现在只使用HolographicDisplays，盔甲架不再属于排行榜
            // 此事件处理器不再需要处理排行榜保护
        }
    }
    
    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        ArmorStand armorStand = event.getRightClicked();
        
        // 由于现在只使用HolographicDisplays，盔甲架不再属于排行榜
        // 此事件处理器不再需要处理排行榜保护
    }
}