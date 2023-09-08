package me.ahmetflix.claim.gui;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.message.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.stream.IntStream;

public class ClaimRemoveMenu {

    private final Component title;
    private final ConfigItem back;
    private final ConfigItem verifyRemoval;
    private final ConfigItem.ItemInfo filler;

    public ClaimRemoveMenu(Component title, ConfigItem back, ConfigItem verifyRemoval, ConfigItem.ItemInfo filler) {
        this.title = title;
        this.back = back;
        this.verifyRemoval = verifyRemoval;
        this.filler = filler;
    }

    public Component getTitle() {
        return title;
    }

    public ConfigItem getBack() {
        return back;
    }

    public ConfigItem getVerifyRemoval() {
        return verifyRemoval;
    }

    public ConfigItem.ItemInfo getFiller() {
        return filler;
    }

    public void open(ClaimData data, Player player) {
        ClaimRemoveMenuGui gui = new ClaimRemoveMenuGui(this, data, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimRemoveMenuGui extends MenuGui {

        private final ClaimRemoveMenu menu;
        private final ClaimData data;

        public ClaimRemoveMenuGui(ClaimRemoveMenu menu, ClaimData data, Player player) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            this.data = data;
            this.menu.back.addToInventory(data, inventory);
            this.menu.verifyRemoval.addToInventory(data, inventory);
            ItemStack filler = menu.getFiller().toItemStack(data);
            IntStream.range(0, inventory.getSize())
                    .filter(i -> inventory.getItem(i) == null)
                    .forEach(i -> inventory.setItem(i, filler));
        }

        public ClaimRemoveMenu getMenu() {
            return menu;
        }

        public ClaimData getData() {
            return data;
        }

        @Override
        public void setClosed(boolean closed) {
            super.setClosed(closed);
            if (closed) {
                this.data.removeMenu(player.getUniqueId());
            }
        }

        @Override
        public void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != this) {
                return;
            }
            if (event.getClickedInventory() == inventory) {
                ItemStack clicked = inventory.getItem(event.getSlot());
                if (clicked == null) return;
                String identifier = clicked.getPersistentDataContainer().get(ConfigItem.IDENTIFIER_KEY, PersistentDataType.STRING);
                if (identifier == null) return;
                if (identifier.equals(menu.back.getIdentifier())) {
                    data.openMenu(player, MenuType.MAIN_CLAIM);
                } else if (identifier.equals(menu.verifyRemoval.getIdentifier())) {
                    player.closeInventory();
                    Messages.CLAIM_DELETED.send(player);
                    FanaClaim.getInstance().getClaimManager().deleteClaim(data.getGriefPreventionClaim());
                }
            }
        }
    }
}
