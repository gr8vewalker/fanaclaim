package me.ahmetflix.claim.command;

import me.ahmetflix.claim.FanaClaim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.isOp()) {
            FanaClaim.getInstance().reloadConfig();
            commandSender.sendMessage(Component.text("Config yeniden yüklendi")
                                              .color(NamedTextColor.GREEN));
        }
        return true;
    }
}
