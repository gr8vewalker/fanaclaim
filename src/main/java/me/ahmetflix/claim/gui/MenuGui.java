package me.ahmetflix.claim.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class MenuGui implements InventoryHolder {

    protected final Player player;
    protected final Inventory inventory;
    protected boolean closed = false;

    public MenuGui(Player player, int size, Component title) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void close() {
        player.closeInventory();
        setClosed(true);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public abstract void onClick(InventoryClickEvent event);

}
