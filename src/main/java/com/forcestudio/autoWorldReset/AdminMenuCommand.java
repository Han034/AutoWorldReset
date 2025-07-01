package com.forcestudio.autoWorldReset;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminMenuCommand implements CommandExecutor {

    private final MenuManager menuManager;

    // Ana sınıftan MenuManager'ı alıyoruz
    public AdminMenuCommand(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player player = (Player) sender;

        // Kendi menü yöneticimizin openAdminMenu metodunu çağırıyoruz!
        menuManager.openAdminMenu(player);

        return true;
    }
}
