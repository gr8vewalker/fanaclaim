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

public class ClaimSettingsMenu {

    private final Component title;
    private final ConfigItem back;
    private final ConfigItem animalEnabled;
    private final ConfigItem animalDisabled;
    private final ConfigItem monsterEnabled;
    private final ConfigItem monsterDisabled;
    private final ConfigItem members;
    private final ConfigItem.ItemInfo filler;

    public ClaimSettingsMenu(Component title, ConfigItem back, ConfigItem animalEnabled, ConfigItem animalDisabled, ConfigItem monsterEnabled, ConfigItem monsterDisabled, ConfigItem members, ConfigItem.ItemInfo filler) {
        this.title = title;
        this.back = back;
        this.animalEnabled = animalEnabled;
        this.animalDisabled = animalDisabled;
        this.monsterEnabled = monsterEnabled;
        this.monsterDisabled = monsterDisabled;
        this.members = members;
        this.filler = filler;
    }

    public Component getTitle() {
        return title;
    }

    public ConfigItem getBack() {
        return back;
    }

    public ConfigItem getAnimalEnabled() {
        return animalEnabled;
    }

    public ConfigItem getAnimalDisabled() {
        return animalDisabled;
    }

    public ConfigItem getMonsterEnabled() {
        return monsterEnabled;
    }

    public ConfigItem getMonsterDisabled() {
        return monsterDisabled;
    }

    public ConfigItem getMembers() {
        return members;
    }

    public ConfigItem.ItemInfo getFiller() {
        return filler;
    }

    public void open(ClaimData data, Player player) {
        ClaimSettingsMenuGui gui = new ClaimSettingsMenuGui(this, data, player);
        player.openInventory(gui.getInventory());
    }

    public static class ClaimSettingsMenuGui extends MenuGui {

        private final ClaimSettingsMenu menu;
        private final ClaimData data;

        public ClaimSettingsMenuGui(ClaimSettingsMenu menu, ClaimData data, Player player) {
            super(player, 27, menu.getTitle());
            this.menu = menu;
            this.data = data;
            createItems();
            IntStream.range(0, inventory.getSize())
                    .filter(i -> inventory.getItem(i) == null)
                    .forEach(i -> inventory.setItem(i, menu.getFiller().toItemStack(data)));
        }

        private void createItems() {
            this.menu.back.addToInventory(data, inventory);
            this.menu.members.addToInventory(data, inventory);
            (data.isMobs() ? this.menu.monsterEnabled : this.menu.monsterDisabled).addToInventory(data, inventory);
            (data.isAnimals() ? this.menu.animalEnabled : this.menu.animalDisabled).addToInventory(data, inventory);
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
                } else if (identifier.equals(menu.members.getIdentifier())) {
                    data.openMenu(player, MenuType.CLAIM_SETTINGS_PLAYERLIST);
                } else if (identifier.equals(menu.monsterEnabled.getIdentifier())) {
                    data.setMobs(false);
                    data.openMenu(player, MenuType.CLAIM_SETTINGS);
                } else if (identifier.equals(menu.monsterDisabled.getIdentifier())) {
                    data.setMobs(true);
                    data.openMenu(player, MenuType.CLAIM_SETTINGS);
                } else if (identifier.equals(menu.animalEnabled.getIdentifier())) {
                    data.setAnimals(false);
                    data.openMenu(player, MenuType.CLAIM_SETTINGS);
                } else if (identifier.equals(menu.animalDisabled.getIdentifier())) {
                    data.setAnimals(true);
                    data.openMenu(player, MenuType.CLAIM_SETTINGS);
                }
            }
        }
    }
}
