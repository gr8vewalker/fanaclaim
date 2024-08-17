package me.ahmetflix.claim.gui;

import it.unimi.dsi.fastutil.Pair;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ClaimExtendMenu {

    private final Component title;
    private final ConfigItem addTime;
    private final ConfigItem removeTime;
    private final ConfigItem confirmExtend;
    private final ConfigItem back;
    private final ConfigItem.ItemInfo filler;

    public ClaimExtendMenu(Component title, ConfigItem addTime, ConfigItem removeTime, ConfigItem confirmExtend, ConfigItem back, ConfigItem.ItemInfo filler) {
        this.title = title;
        this.addTime = addTime;
        this.removeTime = removeTime;
        this.confirmExtend = confirmExtend;
        this.back = back;
        this.filler = filler;
    }

    public Component getTitle() {
        return title;
    }

    public ConfigItem getAddTime() {
        return addTime;
    }

    public ConfigItem getRemoveTime() {
        return removeTime;
    }

    public ConfigItem getConfirmExtend() {
        return confirmExtend;
    }

    public ConfigItem getBack() {
        return back;
    }

    public ConfigItem.ItemInfo getFiller() {
        return filler;
    }

    public void open(ClaimData data, Player player) {
        ClaimExtendMenuGui gui = new ClaimExtendMenuGui(this, data, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimExtendMenuGui extends MenuGui {

        private final ClaimExtendMenu menu;
        private final ClaimData data;
        private int increment;
        private long maxMs;

        public ClaimExtendMenuGui(ClaimExtendMenu menu, ClaimData data, Player player) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            this.data = data;
            this.increment = 0;
            this.maxMs = getMaxMs(player);
            this.menu.addTime.addToInventory(data, inventory);
            this.menu.removeTime.addToInventory(data, inventory);
            this.menu.back.addToInventory(data, inventory);
            ItemStack filler = menu.getFiller().toItemStack();
            IntStream.range(0, inventory.getSize())
                    .filter(i -> inventory.getItem(i) == null)
                    .filter(i -> i != menu.confirmExtend.getSlot())
                    .forEach(i -> inventory.setItem(i, filler));
            generateConfirmItem();
        }

        public void generateConfirmItem() {
            increment = Math.max(increment, 0);
            if (increment == 0) {
                inventory.setItem(menu.confirmExtend.getSlot(), null);
                player.updateInventory();
                return;
            }
            long newMs = data.getEnd() + (increment * 1000 * 60 * 60 * 24L);
            long maxMsCurrent = System.currentTimeMillis() + maxMs;
            if (newMs > maxMsCurrent) increment = (int) ((maxMsCurrent - data.getEnd()) / (1000 * 60 * 60 * 24L));
            int price = (int) (data.getArea() * Settings.EXTEND_TIME_PRICE.getDouble() * increment);
            this.menu.confirmExtend.addToInventoryAmount(inventory, increment, TagResolver.resolver("price", Tag.preProcessParsed(String.valueOf(price))),
                                                   TagResolver.resolver("day", Tag.preProcessParsed(String.valueOf(increment))));
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
                } else if (identifier.equals(menu.addTime.getIdentifier())) {
                    increment++;
                    generateConfirmItem();
                } else if (identifier.equals(menu.removeTime.getIdentifier())) {
                    increment--;
                    generateConfirmItem();
                } else if (identifier.equals(menu.confirmExtend.getIdentifier())) {
                    if (increment > 0) {
                        int price = (int) (data.getArea() * Settings.EXTEND_TIME_PRICE.getDouble() * increment);
                        if (!FanaClaim.getEconomy().has(player, price)) {
                            Messages.NOT_ENOUGH_MONEY.send(player);
                            return;
                        }
                        if (!FanaClaim.getEconomy().withdrawPlayer(player, price).transactionSuccess()) {
                            Messages.INTERNAL_ERROR.send(player);
                            return;
                        }
                        data.renew(increment);
                        Messages.CLAIM_EXTENDED.send(player);
                        data.openMenu(player, MenuType.MAIN_CLAIM);
                    }
                }
            }
        }

        private static long getMaxMs(Player player) {
            long DAY = 1000 * 60 * 60 * 24;
            List<Pair<String, Integer>> claimDaysPermissions = Utils.generatePermissions();
            for (Pair<String, Integer> claimDaysPermission : claimDaysPermissions) {
                if (player.hasPermission(claimDaysPermission.first())) {
                    return claimDaysPermission.second() * DAY;
                }
            }
            return (int)Settings.MAX_CLAIM_DAYS_BY_PERMISSION.getMap().get("default") * DAY;
        }
    }
}
