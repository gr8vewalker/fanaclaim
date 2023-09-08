package me.ahmetflix.claim.gui;

import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.data.Flag;
import me.ahmetflix.claim.data.Flags;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.utils.Utils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

public class ClaimSettingsPlayerMenu {

    private final String title;
    private final ConfigItem player;
    private final ConfigItem back;
    private final ConfigItem.ItemInfo flagEnabled;
    private final ConfigItem.ItemInfo flagDisabled;
    private final ConfigItem.ItemInfo filler;
    private final Map<Flag, Integer> flagSlots;

    public ClaimSettingsPlayerMenu(String title, ConfigItem player, ConfigItem back, ConfigItem.ItemInfo flagEnabled, ConfigItem.ItemInfo flagDisabled, ConfigItem.ItemInfo filler, Map<Flag, Integer> flagSlots) {
        this.title = title;
        this.player = player;
        this.back = back;
        this.flagEnabled = flagEnabled;
        this.flagDisabled = flagDisabled;
        this.filler = filler;
        this.flagSlots = flagSlots;
    }

    public String getTitle() {
        return title;
    }

    public ConfigItem getPlayer() {
        return player;
    }

    public ConfigItem getBack() {
        return back;
    }

    public ConfigItem.ItemInfo getFlagEnabled() {
        return flagEnabled;
    }

    public ConfigItem.ItemInfo getFlagDisabled() {
        return flagDisabled;
    }

    public ConfigItem.ItemInfo getFiller() {
        return filler;
    }

    public Map<Flag, Integer> getFlagSlots() {
        return flagSlots;
    }

    public void open(ClaimData data, Player player, OfflinePlayer editing) {
        ClaimSettingsPlayerMenuGui gui = new ClaimSettingsPlayerMenuGui(this, data, editing, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimSettingsPlayerMenuGui extends MenuGui {

        private final ClaimSettingsPlayerMenu menu;
        private final ClaimData data;
        private final OfflinePlayer editing;

        public ClaimSettingsPlayerMenuGui(ClaimSettingsPlayerMenu menu, ClaimData data, OfflinePlayer editing, Player player) {
            super(player, 54, MiniMessage.miniMessage().deserialize(menu.getTitle(),
                                                                    TagResolver.resolver("player", Tag.preProcessParsed(editing.getName()))));
            this.menu = menu;
            this.data = data;
            this.editing = editing;
            menu.player.addToInventory(inventory,
                                       (stack) -> {
                                           if (stack.getType() == Material.PLAYER_HEAD) {
                                               SkullMeta meta = (SkullMeta) stack.getItemMeta();
                                               Utils.setSkullOwner(meta, editing);
                                               stack.setItemMeta(meta);
                                           }
                                       },
                                       TagResolver.resolver("player", Tag.preProcessParsed(editing.getName())));
            menu.back.addToInventory(inventory);
            populate();
        }

        private void populate() {
            Flags flags = data.getFlags(editing.getUniqueId());
            Arrays.stream(Flag.values())
                    .forEach(flag -> {
                        int slot = menu.flagSlots.get(flag);
                        ConfigItem.ItemInfo info = flags.getFlag(flag) ? menu.flagEnabled : menu.flagDisabled;
                        inventory.setItem(slot, info.toItemStack(flag.name(), Placeholder.component("flag", flag.getMessage().build())));
                    });
            ItemStack filler = menu.filler.toItemStack();
            IntStream.range(0, 54)
                    .filter(i -> inventory.getItem(i) == null)
                    .forEach(i -> inventory.setItem(i, filler));
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
            if (identifier.equals(menu.back.getIdentifier())) data.openMenu(player, MenuType.CLAIM_SETTINGS_PLAYERLIST);
            else {
                Flag flag = Flag.valueOf(identifier);
                Flags flags = data.getFlags(editing.getUniqueId());
                flags.toggleFlag(flag);
                populate();
            }
        }
    }
}
