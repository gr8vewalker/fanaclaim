package me.ahmetflix.claim.listener;

import me.ahmetflix.claim.FanaClaim;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimDeleteListener implements Listener {

    @EventHandler
    public void onClaimDelete(ClaimDeletedEvent event) {
        FanaClaim.getInstance().getClaimManager().removeData(event.getClaim());
    }

}
