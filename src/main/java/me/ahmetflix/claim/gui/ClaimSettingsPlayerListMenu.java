package me.ahmetflix.claim.gui;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

public class ClaimSettingsPlayerListMenu {

    private final Component title;
    private final ConfigItem back;
    private final ConfigItem previous;
    private final ConfigItem next;
    private final ConfigItem.ItemInfo player;

    public ClaimSettingsPlayerListMenu(Component title, ConfigItem back, ConfigItem previous, ConfigItem next, ConfigItem.ItemInfo player) {
        this.title = title;
        this.back = back;
        this.previous = previous;
        this.next = next;
        this.player = player;
    }

    public Component getTitle() {
        return title;
    }

    public ConfigItem getBack() {
        return back;
    }

    public ConfigItem getPrevious() {
        return previous;
    }

    public ConfigItem getNext() {
        return next;
    }

    public ConfigItem.ItemInfo getPlayer() {
        return player;
    }

    public void open(ClaimData data, Player player) {
        ClaimSettingsPlayerListMenuGui gui = new ClaimSettingsPlayerListMenuGui(this, data, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimSettingsPlayerListMenuGui extends MenuGui {

        private final ClaimSettingsPlayerListMenu menu;
        private final ClaimData data;
        private final List<UUID> trustedPlayers;
        private int page = 1;

        public ClaimSettingsPlayerListMenuGui(ClaimSettingsPlayerListMenu menu, ClaimData data, Player player) {
            super(player, 54, menu.getTitle());
            this.menu = menu;
            this.data = data;
            this.trustedPlayers = Utils.getTrustedPlayers(data.getGriefPreventionClaim());
            this.trustedPlayers.remove(player.getUniqueId()); // should not occur but for any case
            populate();
        }

        public void populate() {
            inventory.clear();
            this.menu.back.addToInventory(inventory);
            int pages = (int) Math.ceil(trustedPlayers.size() / 45.0);
            page = Math.max(Math.min(page, pages), 1);
            IntStream.range(0, 45)
                    .forEach(slot -> {
                        int index = (page - 1) * 45 + slot;
                        if (index >= trustedPlayers.size()) return;
                        UUID player = trustedPlayers.get(index);
                        ItemStack item = playerItem(Bukkit.getOfflinePlayer(player));
                        if (item == null) return;
                        inventory.setItem(slot, item);
                    });
            if (pages <= 1) return;
            this.menu.previous.addToInventory(inventory);
            this.menu.next.addToInventory(inventory);
        }

        private ItemStack playerItem(OfflinePlayer player) {
            if (!player.hasPlayedBefore() || player.getName() == null) return null;
            ItemStack stack = this.menu.player.toItemStack(player.getUniqueId().toString(),
                                                           TagResolver.resolver("player", Tag.preProcessParsed(player.getName())));
            if (stack.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) stack.getItemMeta();
                Utils.setSkullOwner(meta, player);
                stack.setItemMeta(meta);
            }
            return stack;
        }

        public void next() {
            int pages = (int) Math.ceil(trustedPlayers.size() / 45.0);
            page = page + 1 > pages ? 1 : page + 1;
            populate();
        }

        public void previous() {
            int pages = (int) Math.ceil(trustedPlayers.size() / 45.0);
            page = page - 1 < 1 ? pages : page - 1;
            populate();
        }

        @Override
        public void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != this) return;
            if (event.getClickedInventory() != inventory) return;
            ItemStack clicked = inventory.getItem(event.getSlot());
            if (clicked == null) return;
            String identifier = clicked.getPersistentDataContainer().get(ConfigItem.IDENTIFIER_KEY, PersistentDataType.STRING);
            if (identifier == null) return;
            if (event.getSlot() < 45) {
                UUID player = UUID.fromString(identifier);
                data.openMenu(this.player, MenuType.CLAIM_SETTINGS_PLAYER, Bukkit.getOfflinePlayer(player));
                return;
            }
            if (identifier.equals(menu.previous.getIdentifier())) previous();
            else if (identifier.equals(menu.next.getIdentifier())) next();
            else if (identifier.equals(menu.back.getIdentifier())) data.openMenu(player, MenuType.CLAIM_SETTINGS);
        }
    }
}
