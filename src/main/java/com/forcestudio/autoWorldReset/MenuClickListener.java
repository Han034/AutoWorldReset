package com.forcestudio.autoWorldReset;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

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
        // Tıklanan envanterin başlığı bizim menümüzün başlığıyla aynı mı?
        if (!event.getView().getTitle().equals(menuManager.ADMIN_MENU_TITLE)) {
            return; // Bizim menümüz değilse, hiçbir şey yapma.
        }

        // Oyuncunun envanterinden bir şey almasını/koymasını engelle.
        event.setCancelled(true);

        // Tıklanan bir eşya var mı ve tıklayan kişi oyuncu mu?
        if (event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Tıklanan slotun numarasına göre işlem yap.
        switch (event.getSlot()) {
            case 11: // Kaynak Dünyası Butonu
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Kaynak dünyası için manuel sıfırlama işlemi başlatılıyor...");
                resetManager.resetWorld("kaynak");
                break;
            case 13: // Maden Dünyası Butonu
                player.closeInventory();
                player.sendMessage(ChatColor.GOLD + "Maden dünyası için manuel sıfırlama işlemi başlatılıyor...");
                resetManager.resetWorld("maden_dunyasi");
                break;
            case 15: // Reload Butonu
                player.closeInventory();
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "AutoWorldReset yapılandırması yeniden yüklendi.");
                break;
            default:
                // Diğer slotlara tıklanırsa bir şey yapma
                break;
        }
    }
}
