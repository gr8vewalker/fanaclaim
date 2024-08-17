package me.ahmetflix.claim.task;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.listener.ClaimDeleteListener;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.settings.Settings;
import me.ryanhamshire.GriefPrevention.DataStore;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class ClaimExpireRunnable extends BukkitRunnable {
    @Override
    public void run() {
        long now = System.currentTimeMillis();
        DataStore dataStore = FanaClaim.getGriefPreventionDataStore();
        FanaClaim.getInstance().getClaimManager().getClaims().stream()
                .filter(data -> now >= data.getEnd())
                .map(data -> dataStore.getClaim(data.getId()))
                .filter(Objects::nonNull)
                .forEach(claim -> {
                    Location loc = claim.getLesserBoundaryCorner();
                    Messages.CLAIM_END
                            .with("player", claim.getOwnerName())
                            .with("world", (String) Settings.CUSTOM_WORLD_NAMES.getMap().get(loc.getWorld().getName()))
                            .with("x", String.valueOf(loc.getX()))
                            .with("y", String.valueOf(loc.getY()))
                            .with("z", String.valueOf(loc.getZ()))
                            .broadcast();
                    ClaimDeleteListener.deletedByBalance.add(claim.getID().longValue());
                    FanaClaim.getInstance().getClaimManager().deleteClaim(claim);
                });
    }
}
