package neko.nekoBwLobby.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import neko.nekoBwLobby.database.DatabaseManager;

import java.util.Arrays;
import java.util.Map;

public class GameInfoGUI {
    
    public static void openGUI(Player player, DatabaseManager databaseManager) {
        // 创建一个3行的箱子GUI
        Inventory gui = Bukkit.createInventory(null, 27, "§8[§b起床战争§8] §f游戏数据");
        
        // 添加装饰性边框
        addDecoration(gui);
        
        // 每次都直接从数据库获取最新的玩家数据，不使用任何缓存
        player.sendMessage("§8[§b起床战争§8] §7正在获取您的游戏数据...");
        Map<String, Object> stats = databaseManager.getPlayerStats(player.getName());
        
        // 创建显示玩家数据的物品
        if (!stats.isEmpty()) {
            player.sendMessage("§8[§b起床战争§8] §a成功获取到您的游戏数据!");
            
            // 添加玩家头像 (1.12.2兼容版本)
            ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            ItemMeta headMeta = playerHead.getItemMeta();
            headMeta.setDisplayName("§f玩家: §b" + player.getName());
            headMeta.setLore(Arrays.asList("§7欢迎查看你的游戏数据", "§7继续加油哦!"));
            playerHead.setItemMeta(headMeta);
            gui.setItem(4, playerHead);
            
            // 击杀数
            ItemStack killsItem = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta killsMeta = killsItem.getItemMeta();
            killsMeta.setDisplayName("§b击杀数");
            killsMeta.setLore(Arrays.asList(
                "§7你已经击败了 §f" + stats.get("kills") + " §7名敌人",
                "",
                "§a§l✓ §7击杀敌人可获得积分"
            ));
            killsItem.setItemMeta(killsMeta);
            gui.setItem(10, killsItem);
            
            // 胜利数
            ItemStack winsItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta winsMeta = winsItem.getItemMeta();
            winsMeta.setDisplayName("§a胜利数");
            winsMeta.setLore(Arrays.asList(
                "§7你已经赢得了 §f" + stats.get("wins") + " §7场游戏",
                "",
                "§a§l✓ §7胜利是最终目标"
            ));
            winsItem.setItemMeta(winsMeta);
            gui.setItem(11, winsItem);
            
            // 分数
            ItemStack scoreItem = new ItemStack(Material.GOLD_INGOT);
            ItemMeta scoreMeta = scoreItem.getItemMeta();
            scoreMeta.setDisplayName("§6分数");
            scoreMeta.setLore(Arrays.asList(
                "§7你的总分数为 §f" + stats.get("score"),
                "",
                "§a§l✓ §7参与游戏即可获得分数"
            ));
            scoreItem.setItemMeta(scoreMeta);
            gui.setItem(12, scoreItem);
            
            // 失败数
            ItemStack losesItem = new ItemStack(Material.REDSTONE);
            ItemMeta losesMeta = losesItem.getItemMeta();
            losesMeta.setDisplayName("§c失败数");
            losesMeta.setLore(Arrays.asList(
                "§7你已经失败了 §f" + stats.get("loses") + " §7次",
                "",
                "§7§l! §7失败是成功之母"
            ));
            losesItem.setItemMeta(losesMeta);
            gui.setItem(13, losesItem);
            
            // 破坏床数
            ItemStack bedsItem = new ItemStack(Material.BED);
            ItemMeta bedsMeta = bedsItem.getItemMeta();
            bedsMeta.setDisplayName("§d破坏床数");
            bedsMeta.setLore(Arrays.asList(
                "§7你已经破坏了 §f" + stats.get("destroyedBeds") + " §7张床",
                "",
                "§a§l✓ §7破坏床是关键策略"
            ));
            bedsItem.setItemMeta(bedsMeta);
            gui.setItem(14, bedsItem);
            
            // 死亡数
            ItemStack deathsItem = new ItemStack(Material.TNT);
            ItemMeta deathsMeta = deathsItem.getItemMeta();
            deathsMeta.setDisplayName("§8死亡数");
            deathsMeta.setLore(Arrays.asList(
                "§7你已经死亡了 §f" + stats.get("deaths") + " §7次",
                "",
                "§7§l! §7小心敌人的攻击"
            ));
            deathsItem.setItemMeta(deathsMeta);
            gui.setItem(15, deathsItem);
        } else {
            player.sendMessage("§8[§b起床战争§8] §c未找到您的游戏数据!");
            
            // 没有找到玩家数据时显示的物品
            ItemStack noDataItem = new ItemStack(Material.BARRIER);
            ItemMeta noDataMeta = noDataItem.getItemMeta();
            noDataMeta.setDisplayName("§c未找到游戏数据");
            noDataMeta.setLore(Arrays.asList(
                "§7看起来你还没有游戏记录",
                "",
                "§7参与一场起床战争游戏来开始记录数据吧!"
            ));
            noDataItem.setItemMeta(noDataMeta);
            gui.setItem(13, noDataItem);
        }
        
        // 打开GUI
        player.openInventory(gui);
    }
    
    /**
     * 为GUI添加装饰性边框
     */
    private static void addDecoration(Inventory gui) {
        // 创建装饰性物品
        ItemStack decoration = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta decorationMeta = decoration.getItemMeta();
        decorationMeta.setDisplayName(" ");
        decoration.setItemMeta(decorationMeta);
        
        // 设置顶部和底部边框
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // 不覆盖玩家头像位置
                gui.setItem(i, decoration.clone());
            }
        }
        for (int i = 18; i < 27; i++) {
            gui.setItem(i, decoration.clone());
        }
        
        // 设置左右边框
        gui.setItem(9, decoration.clone());
        gui.setItem(17, decoration.clone());
    }
}