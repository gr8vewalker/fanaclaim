package me.ahmetflix.claim.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Message {

    private String base;

    public Message(String base) {
        this.base = base;
    }

    public String getBase() {
        return base;
    }

    public void base(String base) {
        this.base = base;
    }

    public void send(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(base));
    }

    public void broadcast() {
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(base));
    }

    public PlaceholderedMessage with(String placeholder, Component value) {
        return new PlaceholderedMessage(this)
                .with(placeholder, value);
    }

    public PlaceholderedMessage with(String placeholder, String value) {
        return new PlaceholderedMessage(this)
                .with(placeholder, value);
    }

    public PlaceholderedMessage with(String placeholder, PlaceholderedMessage value) {
        return new PlaceholderedMessage(this)
                .with(placeholder, value);
    }

    public PlaceholderedMessage with(String placeholder, Object value) {
        return new PlaceholderedMessage(this)
                .with(placeholder, value);
    }
}
