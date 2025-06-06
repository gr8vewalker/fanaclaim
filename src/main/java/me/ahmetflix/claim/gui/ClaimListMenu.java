package me.ahmetflix.claim.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Vector;
import java.util.stream.IntStream;

public class ClaimListMenu {

    private final Component title;
    private final ConfigItem previous;
    private final ConfigItem next;
    private final ConfigItem createNew;
    private final ConfigItem.ItemInfo claimItem;

    public ClaimListMenu(Component title, ConfigItem previous, ConfigItem next, ConfigItem createNew, ConfigItem.ItemInfo claimItem) {
        this.title = title;
        this.previous = previous;
        this.next = next;
        this.createNew = createNew;
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

    public ConfigItem getCreateNew() {
        return createNew;
    }

    public ConfigItem.ItemInfo getClaimItem() {
        return claimItem;
    }

    public void open(Player player) {
        ClaimListMenuGui gui = new ClaimListMenuGui(this, player);
        player.openInventory(gui.getInventory());
    }

    public void openCreation(Player player) {
        ClaimListMenuGui gui = new ClaimListMenuGui(this, player, true);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimListMenuGui extends MenuGui {

        private final ClaimListMenu menu;
        private int page = 1;
        private final Int2ObjectArrayMap<LongArrayList> pageClaims = new Int2ObjectArrayMap<>();
        private final boolean addCreateNew;

        public ClaimListMenuGui(ClaimListMenu menu, Player player) {
            this(menu, player, false);
        }

        public ClaimListMenuGui(ClaimListMenu menu, Player player, boolean addCreateNew) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            this.addCreateNew = addCreateNew;
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
            if (addCreateNew) {
                this.menu.createNew.addToInventory(inventory);
            }
            int pages = pageClaims.size();
            if (pages == 0) return;
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
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != this) return;
            if (event.getClickedInventory() != inventory) return;
            if (event.getSlot() < 18) {
                if (!player.hasPermission("fanaclaim.teleport")) return;
                if (event.getCurrentItem() == null) return;
                int slot = event.getSlot();
                long claimID = pageClaims.get(page).getLong(slot);
                Claim claim = FanaClaim.getGriefPreventionDataStore().getClaim(claimID);
                Messages.CLAIM_TELEPORTING.send(player);
                Utils.teleportSafeLocationNoMove(player, Utils.middleCornerLocation(claim), 20 * 3L);
                player.closeInventory();
                return;
            }
            ItemStack clicked = inventory.getItem(event.getSlot());
            if (clicked == null) return;
            String identifier = clicked.getPersistentDataContainer().get(ConfigItem.IDENTIFIER_KEY, PersistentDataType.STRING);
            if (identifier == null) return;
            if (identifier.equals(menu.previous.getIdentifier())) previous();
            else if (identifier.equals(menu.next.getIdentifier())) next();
            else if (identifier.equals(menu.createNew.getIdentifier())) {
                player.closeInventory();
                GriefPrevention.getPlugin(GriefPrevention.class).onCommand(player, Bukkit.getPluginCommand("claim"), "claim", new String[0]);
            }
        }
    }

}
