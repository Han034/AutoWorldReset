package com.forcestudio.autoWorldReset;

import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    private final AutoWorldReset plugin;
    private final WorldResetManager resetManager;

    public WorldChangeListener(AutoWorldReset plugin, WorldResetManager resetManager) {
        this.plugin = plugin;
        this.resetManager = resetManager;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // --- BOSSBAR YÖNETİMİ (Bu kısım aynı kalıyor) ---
        // Oyuncunun ayrıldığı dünyayı kontrol et
        BossBar fromWorldBar = resetManager.getActiveBossBar(event.getFrom().getName());
        if (fromWorldBar != null) {
            fromWorldBar.removePlayer(player);
        }

        // Oyuncunun girdiği dünyayı kontrol et
        BossBar toWorldBar = resetManager.getActiveBossBar(player.getWorld().getName());
        if (toWorldBar != null) {
            toWorldBar.addPlayer(player);
        }

        // --- YENİ: SPAWN KORUMASI MANTIĞI ---
        // Oyuncunun girdiği dünya, sıfırlanmak üzere ayarlanmış bir dünya mı?
        if (plugin.getConfig().isConfigurationSection("worlds-to-reset." + player.getWorld().getName())) {
            int protectionSeconds = plugin.getConfig().getInt("spawn-protection-seconds", 0);
            if (protectionSeconds > 0) {
                // Oyuncuyu koruma listesine ekle ve süreyi başlat
                resetManager.addProtectedPlayer(player, protectionSeconds);
            }
        }
    }
}
