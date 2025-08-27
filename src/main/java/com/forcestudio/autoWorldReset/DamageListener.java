package com.forcestudio.autoWorldReset;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    private final WorldResetManager resetManager;

    public DamageListener(WorldResetManager resetManager) {
        this.resetManager = resetManager;
    }

    // EventPriority.HIGHEST: Diğer eklentilerin kararından sonra son sözü söylemek için.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        // Hasar alan bir oyuncu mu?
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Bu oyuncu koruma listesinde mi?
        if (resetManager.isPlayerProtected(player)) {
            // Evet, listede. Hasarı iptal et.
            event.setCancelled(true);

            // Oyuncuya neden hasar almadığını bildirebiliriz (isteğe bağlı)
            // Bu mesajın sürekli spam olmaması için bir zaman aşımı eklenebilir.
            // Şimdilik basit tutuyoruz.
            // player.sendMessage(ChatColor.GREEN + "Spawn korumanız aktif olduğu için hasar almadınız!");
        }
    }
}