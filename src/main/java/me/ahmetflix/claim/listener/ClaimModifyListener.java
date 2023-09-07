package me.ahmetflix.claim.listener;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimModifyListener implements Listener {

    @EventHandler
    public void onModify(ClaimModifiedEvent event) {
        ClaimData claimData = FanaClaim.getInstance().getClaimManager().getClaim(event.getTo().getID());
        if (claimData != null) {
            Claim claim = event.getTo();
            claimData.modify((int) Utils.calculateArea(claim));
        }
    }

}
