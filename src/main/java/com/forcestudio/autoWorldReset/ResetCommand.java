package com.forcestudio.autoWorldReset;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ResetCommand implements CommandExecutor {

    private final AutoWorldReset plugin;
    private final WorldResetManager resetManager;

    // Ana sınıftan ve manager'dan referans alıyoruz
    public ResetCommand(AutoWorldReset plugin, WorldResetManager resetManager) {
        this.plugin = plugin;
        this.resetManager = resetManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Komutu kullananın izni var mı?
        if (!sender.hasPermission("worldresetter.admin")) {
            sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok.");
            return true;
        }

        // Komut argümanlarını kontrol et
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Kullanım: /worldreset <reload|reset|info>");
            return true;
        }

        // /worldreset reload
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "WorldResetter yapılandırma dosyası yeniden yüklendi.");
            return true;
        }

        // /worldreset reset <dünya_adı>
        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Kullanım: /worldreset reset <dünya_adı>");
                return true;
            }
            String worldName = args[1];
            sender.sendMessage(ChatColor.GOLD + worldName + " dünyası için manuel sıfırlama işlemi başlatılıyor...");
            resetManager.resetWorld(worldName); // Sıfırlama işlemini yöneticiye devret
            return true;
        }

        // /worldreset info <dünya_adı>
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.YELLOW + "Kullanım: /worldreset info <dünya_adı>");
                return true;
            }
            String worldName = args[1];
            resetManager.sendWorldInfo(sender, worldName); // Bilgi isteme işlemini yöneticiye devret
            return true;
        }


        sender.sendMessage(ChatColor.YELLOW + "Kullanım: /worldreset <reload|reset|info>");
        return true;
    }
}
