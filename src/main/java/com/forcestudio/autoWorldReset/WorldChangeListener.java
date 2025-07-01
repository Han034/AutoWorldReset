package com.forcestudio.autoWorldReset;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeListener implements Listener {

    private final WorldResetManager resetManager;

    public WorldChangeListener(WorldResetManager resetManager) {
        this.resetManager = resetManager;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

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
    }
}
