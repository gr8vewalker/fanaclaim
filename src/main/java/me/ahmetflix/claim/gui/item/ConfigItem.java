package me.ahmetflix.claim.gui.item;

import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConfigItem implements ConfigurationSerializable {

    public static final NamespacedKey IDENTIFIER_KEY = new NamespacedKey(FanaClaim.getInstance(), "identifier");

    private final int slot;
    private final boolean update;
    private final String identifier;
    private final ItemInfo itemInfo;

    public ConfigItem(int slot, boolean update, String identifier, ItemInfo itemInfo) {
        this.slot = slot;
        this.update = update;
        this.identifier = identifier;
        this.itemInfo = itemInfo;
    }

    public int getSlot() {
        return slot;
    }

    public boolean shouldUpdate() {
        return update;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public void addToInventory(Inventory inventory, Consumer<ItemStack> editor, TagResolver... resolvers) {
        ItemStack stack = itemInfo.toItemStack(identifier, resolvers);
        editor.accept(stack);
        inventory.setItem(slot, stack);
    }

    public void addToInventory(ClaimData data, Inventory inventory) {
        inventory.setItem(slot, itemInfo.toItemStack(identifier, data));
    }

    public void addToInventory(Inventory inventory, TagResolver... resolvers) {
        inventory.setItem(slot, itemInfo.toItemStack(identifier, resolvers));
    }

    public void addToInventoryAmount(Inventory inventory, int amount, TagResolver... resolvers) {
        ItemStack itemStack = itemInfo.toItemStack(identifier, resolvers);
        itemStack.setAmount(amount);
        inventory.setItem(slot, itemStack);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("slot", slot);
        map.put("item", itemInfo);
        map.put("update", update);
        map.put("identifier", identifier);
        return map;
    }

    public static ConfigItem deserialize(Map<String, Object> map) {
        int slot = (int) map.get("slot");
        boolean update = (boolean) map.get("update");
        String identifier = (String) map.get("identifier");
        ItemInfo itemInfo = (ItemInfo) map.get("item");
        return new ConfigItem(slot, update, identifier, itemInfo);
    }

    public static class ItemInfo implements ConfigurationSerializable {

        private final Material material;
        private final String name;
        private final List<String> lore;

        public ItemInfo(Material material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }


        @Override
        public @NotNull Map<String, Object> serialize() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("material", material.name());
            map.put("name", name);
            map.put("lore", lore);
            return map;
        }

        public static ItemInfo deserialize(Map<String, Object> map) {
            Material material = Material.valueOf((String) map.get("material"));
            String name = (String) map.get("name");
            List<String> lore = (List<String>) map.get("lore");
            return new ItemInfo(material, name, lore);
        }

        public ItemStack toItemStack(ClaimData data) {
            return toItemStack(null, data.createTagResolvers());
        }

        public ItemStack toItemStack(TagResolver... tagResolvers) {
            return toItemStack(null, tagResolvers);
        }

        public ItemStack toItemStack(String identifier, ClaimData data) {
            return toItemStack(identifier, data.createTagResolvers());
        }

        public ItemStack toItemStack(String identifier, TagResolver... tagResolvers) {
            ItemStack itemStack = new ItemStack(material);
            itemStack.editMeta(meta -> {
                meta.displayName(MiniMessage.miniMessage().deserialize(name, tagResolvers).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore.stream()
                                  .map(line -> MiniMessage.miniMessage().deserialize(line, tagResolvers).decoration(TextDecoration.ITALIC, false))
                                  .collect(Collectors.toList()));
                if (identifier != null) meta.getPersistentDataContainer().set(IDENTIFIER_KEY, PersistentDataType.STRING, identifier);
            });
            return itemStack;
        }
    }
}
