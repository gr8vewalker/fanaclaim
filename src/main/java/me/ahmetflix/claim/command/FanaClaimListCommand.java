package me.ahmetflix.claim.command;

import me.ahmetflix.claim.FanaClaim;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FanaClaimListCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        FanaClaim.getInstance().getClaimListMenu().open((Player) sender);
        return true;
    }
}
