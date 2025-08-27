package com.forcestudio.autoWorldReset;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MenuManager {

    private final AutoWorldReset plugin;
    private final WorldResetManager resetManager;
    public final String ADMIN_MENU_TITLE = ChatColor.DARK_RED + "» " + ChatColor.BOLD + "Dünya Sıfırlama Paneli";

    // --- YENİ ---
    // Eşyaların içine veri gömmek için kullanacağımız benzersiz anahtar.
    public static final NamespacedKey WORLD_NAME_KEY = new NamespacedKey("autoworldreset", "world_name");
    public static final NamespacedKey ACTION_KEY = new NamespacedKey("autoworldreset", "action");


    public MenuManager(WorldResetManager resetManager, AutoWorldReset plugin) {
        this.resetManager = resetManager;
        this.plugin = plugin;
    }

    // --- GÜNCELLENDİ ---
    // Oyuncuya yönetici menüsünü açan ana metot
    public void openAdminMenu(Player player) {
        Inventory adminMenu = Bukkit.createInventory(null, 54, ADMIN_MENU_TITLE); // Boyutu 6 satıra çıkardık

        // config.yml'den sıfırlanacak dünyaların listesini al
        ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds-to-reset");
        int currentSlot = 10; // Eşyaları dizmeye başlayacağımız slot

        if (worldsSection != null) {
            Set<String> worldNames = worldsSection.getKeys(false);

            // Her bir dünya için bir buton oluştur ve menüye ekle
            for (String worldName : worldNames) {
                if (currentSlot >= 35) break; // Menü dolarsa döngüyü kır (sınır)
                adminMenu.setItem(currentSlot, createWorldItem(
                        getMaterialForWorld(worldName), // Dünyaya göre rastgele bir materyal seç
                        "&a" + worldName + " Yönetimi",
                        worldName
                ));
                currentSlot++;
            }
        }

        // --- Sabit Butonlar ---
        adminMenu.setItem(48, createSimpleItem(
                Material.REPEATER,
                "&eEklenti Ayarlarını Yeniden Yükle",
                Collections.singletonList("&7config.yml dosyasını yeniden yükler."),
                "reload_config" // Bu butona özel bir eylem etiketi
        ));

        adminMenu.setItem(50, createSimpleItem(
                Material.BARRIER,
                "&cMenüyü Kapat",
                Collections.emptyList(),
                "close_menu" // Bu butona özel bir eylem etiketi
        ));

        player.openInventory(adminMenu);
    }

    // --- GÜNCELLENDİ ---
    // createWorldItem metodu artık veri etiketleme yapıyor
    private ItemStack createWorldItem(Material material, String name, String worldName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Bu dünya için sıfırlama bilgilerini");
            lore.add(ChatColor.GRAY + "görüntüleyin ve yönetin.");
            lore.add(" ");
            lore.add(ChatColor.YELLOW + "Son Sıfırlanma: " + ChatColor.WHITE + resetManager.getLastResetFormatted(worldName));
            lore.add(ChatColor.YELLOW + "Sonraki Sıfırlanma: " + ChatColor.WHITE + resetManager.getNextResetFormatted(worldName));
            lore.add(ChatColor.YELLOW + "Kalan Süre: " + ChatColor.WHITE + resetManager.getRemainingTimeFormatted(worldName));
            lore.add(" ");
            lore.add(ChatColor.RED + "" + ChatColor.BOLD + "Sıfırlamak için Tıklayın!");
            meta.setLore(lore);

            // --- YENİ & ÖNEMLİ ---
            // Eşyanın içine görünmez bir şekilde dünya adını "etiketliyoruz".
            meta.getPersistentDataContainer().set(WORLD_NAME_KEY, PersistentDataType.STRING, worldName);

            item.setItemMeta(meta);
        }
        return item;
    }

    // --- GÜNCELLENDİ ---
    // createSimpleItem metodu artık eylem etiketleme yapıyor
    private ItemStack createSimpleItem(Material material, String name, List<String> lore, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            // Bu butona da bir "eylem" etiketi ekliyoruz.
            meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);

            item.setItemMeta(meta);
        }
        return item;
    }

    // Dünyanın adına göre basit bir ikon seçen yardımcı metot
    private Material getMaterialForWorld(String worldName) {
        if (worldName.contains("nether")) return Material.NETHERRACK;
        if (worldName.contains("end")) return Material.END_STONE;
        if (worldName.contains("maden")) return Material.DEEPSLATE_DIAMOND_ORE;
        return Material.GRASS_BLOCK;
    }
}