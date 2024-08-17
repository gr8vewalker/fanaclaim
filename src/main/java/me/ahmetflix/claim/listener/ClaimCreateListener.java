package me.ahmetflix.claim.listener;

import me.ahmetflix.claim.FanaClaim;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimCreateListener implements Listener {

    @EventHandler
    public void onClaimCreate(ClaimCreatedEvent event) {
        Bukkit.getScheduler().runTaskLater(FanaClaim.getInstance(), () -> {
            long claimId = event.getClaim().getID();
            FanaClaim.getInstance().getClaimManager().addClaim(claimId);
        }, 1L);
    }

}
