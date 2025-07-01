package com.forcestudio.autoWorldReset;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuManager {

    private final WorldResetManager resetManager;
    public final String ADMIN_MENU_TITLE = ChatColor.DARK_RED + "» " + ChatColor.BOLD + "Dünya Sıfırlama Paneli";

    public MenuManager(WorldResetManager resetManager) {
        this.resetManager = resetManager;
    }

    // Oyuncuya yönetici menüsünü açan ana metot
    public void openAdminMenu(Player player) {
        // Bukkit.createInventory ile sanal bir sandık oluşturuyoruz.
        // null: Envanterin sahibi yok (standart bir envanter).
        // 27: Boyutu (3 satır).
        // ADMIN_MENU_TITLE: Başlığı. Bu başlık, tıklama olaylarını dinlerken menüyü tanımamızı sağlayacak.
        Inventory adminMenu = Bukkit.createInventory(null, 27, ADMIN_MENU_TITLE);

        // Menüdeki butonları oluşturup envantere ekliyoruz.
        // --- KAYNAK DÜNYASI BUTONU ---
        // Bu metot, PlaceholderAPI'nin yaptığı gibi, dinamik bilgileri alıp bir eşyaya dönüştürür.
        adminMenu.setItem(11, createWorldItem(
                Material.GRASS_BLOCK,
                "&aKaynak Dünyası Yönetimi",
                "kaynak_dunyasi"
        ));

        // --- MADEN DÜNYASI BUTONU ---
        adminMenu.setItem(13, createWorldItem(
                Material.DEEPSLATE_DIAMOND_ORE,
                "&bMaden Dünyası Yönetimi",
                "maden_dunyasi"
        ));

        // --- RELOAD BUTONU ---
        adminMenu.setItem(15, createSimpleItem(
                Material.REPEATER,
                "&eEklenti Ayarlarını Yeniden Yükle",
                Collections.singletonList("&7config.yml dosyasını yeniden yükler.")
        ));

        // Oyuncuya bu oluşturduğumuz envanteri gösteriyoruz.
        player.openInventory(adminMenu);
    }

    // Dünyaya özel, dinamik bilgi içeren bir buton oluşturan yardımcı metot
    private ItemStack createWorldItem(Material material, String name, String worldName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            // Lore (alt yazı) kısmını dinamik olarak WorldResetManager'dan gelen bilgiyle dolduruyoruz.
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Bu dünya için sıfırlama bilgilerini");
            lore.add(ChatColor.GRAY + "görüntüleyin ve yönetin.");
            lore.add(" "); // Boş satır
            lore.add(ChatColor.YELLOW + "Son Sıfırlanma: " + ChatColor.WHITE + resetManager.getLastResetFormatted(worldName));
            lore.add(ChatColor.YELLOW + "Sonraki Sıfırlanma: " + ChatColor.WHITE + resetManager.getNextResetFormatted(worldName));
            lore.add(ChatColor.YELLOW + "Kalan Süre: " + ChatColor.WHITE + resetManager.getRemainingTimeFormatted(worldName));
            lore.add(" ");
            lore.add(ChatColor.RED + "" + ChatColor.BOLD + "Sıfırlamak için Tıklayın!");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Basit, statik bir buton oluşturan yardımcı metot
    private ItemStack createSimpleItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}