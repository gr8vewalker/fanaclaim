package me.ahmetflix.claim.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Vector;
import java.util.stream.IntStream;

public class ClaimListMenu {

    private final Component title;
    private final ConfigItem previous;
    private final ConfigItem next;
    private final ConfigItem.ItemInfo claimItem;

    public ClaimListMenu(Component title, ConfigItem previous, ConfigItem next, ConfigItem.ItemInfo claimItem) {
        this.title = title;
        this.previous = previous;
        this.next = next;
        this.claimItem = claimItem;
    }

    public Component getTitle() {
        return title;
    }

    public ConfigItem getPrevious() {
        return previous;
    }

    public ConfigItem getNext() {
        return next;
    }

    public ConfigItem.ItemInfo getClaimItem() {
        return claimItem;
    }

    public void open(Player player) {
        ClaimListMenuGui gui = new ClaimListMenuGui(this, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimListMenuGui extends MenuGui {

        private final ClaimListMenu menu;
        private int page = 1;
        private final Int2ObjectArrayMap<LongArrayList> pageClaims = new Int2ObjectArrayMap<>();

        public ClaimListMenuGui(ClaimListMenu menu, Player player) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            // 18 claim per page
            PlayerData playerData = FanaClaim.getGriefPreventionDataStore().getPlayerData(player.getUniqueId());
            Vector<Claim> claims = playerData.getClaims();
            int size = claims.size();
            int pages = (int) Math.ceil(size / 18.0);
            for (int i = 0; i < pages; i++) {
                LongArrayList list = new LongArrayList();
                int index = i * 18;
                for (int j = index; j < index + 18; j++) {
                    if (j >= size) {
                        break;
                    }
                    Claim claim = claims.get(j);
                    list.add(claim.getID().longValue());
                }
                pageClaims.put(i + 1, list);
            }
            populate();
        }

        public ClaimListMenu getMenu() {
            return menu;
        }

        public void populate() {
            inventory.clear();
            int pages = pageClaims.size();
            page = Math.max(Math.min(page, pages), 1);
            LongArrayList list = pageClaims.get(page);
            ObjectArrayList<ItemStack> items = new ObjectArrayList<>();
            list.forEach(claimID -> items.add(this.menu.claimItem.toItemStack(FanaClaim.getInstance().getClaimManager().getClaim(claimID))));
            IntStream.range(0, 18)
                    .forEach(i -> inventory.setItem(i, i < items.size() ? items.get(i) : null));
            if (pages == 1) return;
            this.menu.previous.addToInventory(inventory);
            this.menu.next.addToInventory(inventory);
        }

        public void next() {
            int pages = pageClaims.size();
            page = page + 1 > pages ? 1 : page + 1;
            populate();
        }

        public void previous() {
            int pages = pageClaims.size();
            page = page - 1 < 1 ? pages : page - 1;
            populate();
        }

        @Override
        public void onClick(InventoryClickEvent event) {
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != this) return;
            event.setCancelled(true);
            if (event.getClickedInventory() != inventory) return;
            if (event.getSlot() < 18) {
                if (!player.hasPermission("fanaclaim.teleport")) return;
                if (event.getCurrentItem() == null) return;
                int slot = event.getSlot();
                long claimID = pageClaims.get(page).getLong(slot);
                Claim claim = FanaClaim.getGriefPreventionDataStore().getClaim(claimID);
                Utils.teleportSafeLocation(player, Utils.middleCornerLocation(claim));
                return;
            }
            ConfigItem clicked = menu.next.getSlot() == event.getSlot()
                                 ? menu.next
                                 : menu.previous.getSlot() == event.getSlot()
                                   ? menu.previous
                                   : null;
            if (clicked == null) return;
            String identifier = clicked.getIdentifier();
            if (identifier.equals("next")) next();
            else if (identifier.equals("previous")) previous();
        }
    }

}
