package com.forcestudio.autoWorldReset;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AutoWorldResetExpansion extends PlaceholderExpansion {

    private final AutoWorldReset plugin;
    private final WorldResetManager resetManager;

    public AutoWorldResetExpansion(AutoWorldReset plugin, WorldResetManager resetManager) {
        this.plugin = plugin;
        this.resetManager = resetManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        // Placeholder'ımızın ana kimliği. %autoworldreset_...% şeklinde kullanılacak.
        return "autoworldreset";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // Eklenti reload olduğunda bu placeholder'ın aktif kalmasını sağlar.
        return true;
    }

    // Gelen placeholder isteğini işleyen metot
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // params: placeholder'ın alt kısmı. Örn: "kalan_süre_kaynak_dunyasi"
        String[] parts = params.split("_");

        if (parts.length < 2) return null; // Geçersiz format

        String type = parts[0];
        // Kalan kısmı birleştirerek dünya adını alıyoruz. Örn: kalan_süre_DÜNYA_ADI -> DÜNYA_ADI
        String worldName = params.substring(type.length() + 1);

        switch (type.toLowerCase()) {
            case "kalan":
                return resetManager.getRemainingTimeFormatted(worldName);
            case "son":
                return resetManager.getLastResetFormatted(worldName);
            case "sonraki":
                return resetManager.getNextResetFormatted(worldName);
        }

        return null; // Eğer aranan placeholder bulunamazsa null döndür.
    }
}