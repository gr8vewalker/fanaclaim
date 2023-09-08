package me.ahmetflix.claim.command;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.gui.MenuType;
import me.ahmetflix.claim.message.Messages;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FanaClaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (sender.isOp() && args.length > 0) {
            String arg = args[0];
            if (arg.equalsIgnoreCase("delete")) {
                // oyuncunun üstünde bulunduğu claimi siler
                Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
                FanaClaim.getInstance().getClaimManager().deleteClaim(claim);
                return true;
            }
            if (args.length > 1) {
                if (arg.equalsIgnoreCase("add")) {
                    // oyuncunun üstünde bulunduğu claime süre ekler (birim: saat)
                    Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
                    ClaimData data = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
                    String time = args[1];
                    try {
                        int hour = Integer.parseInt(time);
                        data.renew(hour / 24D);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Hatalı bir süre girdiniz.");
                    }
                    return true;
                } else if (arg.equalsIgnoreCase("remove")) {
                    Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
                    ClaimData data = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
                    String time = args[1];
                    try {
                        int hour = Integer.parseInt(time);
                        data.subtract(hour / 24D);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Hatalı bir süre girdiniz.");
                    }
                    return true;
                }
            }
        }
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(player.getLocation(), false, null);
        if (claim == null) {
            Messages.NO_CLAIM.send(player);
            return true;
        }
        if (!claim.ownerID.equals(player.getUniqueId())) {
            Messages.NOT_OWNER.send(player);
            return true;
        }
        ClaimData data = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        data.openMenu(player, MenuType.MAIN_CLAIM);
        return true;
    }
}
