package com.forcestudio.autoWorldReset;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MenuClickListener implements Listener {

    private final MenuManager menuManager;
    private final WorldResetManager resetManager;
    private final AutoWorldReset plugin;

    public MenuClickListener(MenuManager menuManager, WorldResetManager resetManager, AutoWorldReset plugin) {
        this.menuManager = menuManager;
        this.resetManager = resetManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(menuManager.ADMIN_MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        // --- YENİ & AKILLI TIKLAMA MANTIĞI ---

        // 1. Tıklanan eşyanın içinde "dünya adı" etiketi var mı?
        if (meta.getPersistentDataContainer().has(MenuManager.WORLD_NAME_KEY, PersistentDataType.STRING)) {
            String worldName = meta.getPersistentDataContainer().get(MenuManager.WORLD_NAME_KEY, PersistentDataType.STRING);

            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + worldName + " dünyası için manuel sıfırlama işlemi başlatılıyor...");
            resetManager.resetWorld(worldName);
            return; // İşlem bitti.
        }

        // 2. Eğer dünya adı etiketi yoksa, "eylem" etiketi var mı?
        if (meta.getPersistentDataContainer().has(MenuManager.ACTION_KEY, PersistentDataType.STRING)) {
            String action = meta.getPersistentDataContainer().get(MenuManager.ACTION_KEY, PersistentDataType.STRING);

            switch (action) {
                case "reload_config":
                    player.closeInventory();
                    plugin.reloadConfig();
                    player.sendMessage(ChatColor.GREEN + "AutoWorldReset yapılandırması yeniden yüklendi.");
                    break;
                case "close_menu":
                    player.closeInventory();
                    break;
            }
        }
    }
}
