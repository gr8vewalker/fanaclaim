package me.ahmetflix.claim.gui;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.gui.item.ConfigItem;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiUpdaterTask {

    public static void run(ClaimMenu.ClaimMenuGui claimMenuGui) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (claimMenuGui.isClosed()) cancel();
                else {
                    claimMenuGui.getMenu().getItems().stream()
                            .filter(ConfigItem::shouldUpdate)
                            .forEach(item -> item.addToInventory(claimMenuGui.getData(), claimMenuGui.getInventory()));
                }
            }
        }.runTaskTimer(FanaClaim.getInstance(), 20, 20);
    }
}
