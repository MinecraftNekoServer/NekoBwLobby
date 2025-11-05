package neko.nekoBwLobby.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemProtectionListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            // 如果玩家是创造模式，允许操作
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            
            // 检查是否点击的是玩家自己的物品栏
            if (event.getClickedInventory() != null && 
                event.getClickedInventory().equals(player.getInventory())) {
                // 取消事件，防止移动物品
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家是创造模式，允许丢弃物品
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        // 取消事件，防止丢弃物品
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家是创造模式，允许拾取物品
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        
        // 取消事件，防止拾取物品
        event.setCancelled(true);
    }
}