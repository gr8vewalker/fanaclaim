package me.ahmetflix.claim.listener;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractionListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Claim claim = FanaClaim.getGriefPreventionDataStore().getClaimAt(block.getLocation(), true, null);
        if (claim == null) {
            return;
        }
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(claim.getID());
        if (claimData == null) {
            return;
        }
        Player player = event.getPlayer();
        Messages.UNTIL_CLAIM_ENDS
                .with("player", player.getName())
                .with("remaining", Utils.convertToMessage(claimData.getEnd() - System.currentTimeMillis()))
                .with("world", (String) Settings.CUSTOM_WORLD_NAMES.getMap().get(block.getWorld().getName()))
                .with("x", String.valueOf(block.getX()))
                .with("y", String.valueOf(block.getY()))
                .with("z", String.valueOf(block.getZ()))
                .send(player);
    }

}
