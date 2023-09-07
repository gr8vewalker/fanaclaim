package me.ahmetflix.claim.gui;

import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.gui.item.ConfigItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.IntStream;

public class ClaimMenu {

    private final Component title;
    private final List<ConfigItem> items;
    private final ConfigItem.ItemInfo filler;

    public ClaimMenu(Component title, List<ConfigItem> items, ConfigItem.ItemInfo filler) {
        this.title = title;
        this.items = items;
        this.filler = filler;
    }

    public Component getTitle() {
        return title;
    }

    public List<ConfigItem> getItems() {
        return items;
    }

    public ConfigItem.ItemInfo getFiller() {
        return filler;
    }

    public void open(ClaimData data, Player player) {
        ClaimMenuGui gui = new ClaimMenuGui(this, data, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimMenuGui extends MenuGui {

        private final ClaimMenu menu;
        private final ClaimData data;

        public ClaimMenuGui(ClaimMenu menu, ClaimData data, Player player) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            this.data = data;
            this.menu.getItems().forEach(item -> item.addToInventory(data, inventory));
            ItemStack filler = menu.getFiller().toItemStack(data);
            IntStream.range(0, inventory.getSize())
                    .filter(i -> inventory.getItem(i) == null)
                    .forEach(i -> inventory.setItem(i, filler));
            GuiUpdaterTask.run(this);
        }

        public ClaimMenu getMenu() {
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
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != this) {
                return;
            }
            event.setCancelled(true);
            if (event.getClickedInventory() == inventory) {
                ConfigItem item = menu.getItems().stream()
                        .filter(i -> i.getSlot() == event.getSlot())
                        .findFirst()
                        .orElse(null);
                if (item != null) {
                    String identifier = item.getIdentifier();
                    MenuType toOpen = MenuType.getById(identifier);
                    if (toOpen != null) {
                        data.openMenu(player, toOpen);
                    }
                }
            }
        }
    }

}
