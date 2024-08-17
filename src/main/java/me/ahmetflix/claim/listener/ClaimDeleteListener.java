package me.ahmetflix.claim.listener;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimDeleteListener implements Listener {

    public static LongArrayList deletedByBalance = new LongArrayList();

    @EventHandler
    public void onClaimDelete(ClaimDeletedEvent event) {
        long id = event.getClaim().getID();
        if (!deletedByBalance.contains(id)) {
            int price = (int) (Utils.calculateArea(event.getClaim()) * Settings.DELETE_PENALTY.getDouble());
            FanaClaim.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(event.getClaim().getOwnerID()), price);
        }
        FanaClaim.getInstance().getClaimManager().removeData(event.getClaim());
    }

}
