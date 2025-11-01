package neko.nekoBwLobby.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import neko.nekoBwLobby.database.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public class GameInfoGUI {
    
    public static void openGUI(Player player, DatabaseManager databaseManager) {
        // 创建一个3行的箱子GUI
        Inventory gui = Bukkit.createInventory(null, 27, "起床战争游戏数据");
        
        // 获取玩家数据
        Map<String, Object> stats = databaseManager.getPlayerStats(player.getName());
        
        // 创建显示玩家数据的物品
        if (!stats.isEmpty()) {
            // 击杀数
            ItemStack killsItem = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta killsMeta = killsItem.getItemMeta();
            killsMeta.setDisplayName("§b击杀数: §f" + stats.get("kills"));
            killsItem.setItemMeta(killsMeta);
            gui.setItem(10, killsItem);
            
            // 胜利数
            ItemStack winsItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta winsMeta = winsItem.getItemMeta();
            winsMeta.setDisplayName("§a胜利数: §f" + stats.get("wins"));
            winsItem.setItemMeta(winsMeta);
            gui.setItem(11, winsItem);
            
            // 分数
            ItemStack scoreItem = new ItemStack(Material.GOLD_INGOT);
            ItemMeta scoreMeta = scoreItem.getItemMeta();
            scoreMeta.setDisplayName("§6分数: §f" + stats.get("score"));
            scoreItem.setItemMeta(scoreMeta);
            gui.setItem(12, scoreItem);
            
            // 失败数
            ItemStack losesItem = new ItemStack(Material.REDSTONE);
            ItemMeta losesMeta = losesItem.getItemMeta();
            losesMeta.setDisplayName("§c失败数: §f" + stats.get("loses"));
            losesItem.setItemMeta(losesMeta);
            gui.setItem(13, losesItem);
            
            // 破坏床数
            ItemStack bedsItem = new ItemStack(Material.RED_BED);
            ItemMeta bedsMeta = bedsItem.getItemMeta();
            bedsMeta.setDisplayName("§d破坏床数: §f" + stats.get("destroyedBeds"));
            bedsItem.setItemMeta(bedsMeta);
            gui.setItem(14, bedsItem);
            
            // 死亡数
            ItemStack deathsItem = new ItemStack(Material.TNT);
            ItemMeta deathsMeta = deathsItem.getItemMeta();
            deathsMeta.setDisplayName("§8死亡数: §f" + stats.get("deaths"));
            deathsItem.setItemMeta(deathsMeta);
            gui.setItem(15, deathsItem);
        } else {
            // 没有找到玩家数据时显示的物品
            ItemStack noDataItem = new ItemStack(Material.BARRIER);
            ItemMeta noDataMeta = noDataItem.getItemMeta();
            noDataMeta.setDisplayName("§c未找到游戏数据");
            noDataItem.setItemMeta(noDataMeta);
            gui.setItem(13, noDataItem);
        }
        
        // 打开GUI
        player.openInventory(gui);
    }
}