package me.ahmetflix.claim.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PlaceholderedMessage {

    private final Message message;
    private final ObjectArrayList<TagResolver> resolvers = new ObjectArrayList<>();

    public PlaceholderedMessage(Message message) {
        this.message = message;
    }

    public PlaceholderedMessage with(String placeholder, Component value) {
        resolvers.add(Placeholder.component(placeholder, value));
        return this;
    }

    public PlaceholderedMessage with(String placeholder, String value) {
        resolvers.add(TagResolver.resolver(placeholder, Tag.preProcessParsed(value)));
        return this;
    }

    public PlaceholderedMessage with(String placeholder, PlaceholderedMessage value) {
        resolvers.add(Placeholder.component(placeholder, value.build()));
        return this;
    }

    public PlaceholderedMessage with(String placeholder, Object value) {
        if (value instanceof Component) {
            return with(placeholder, (Component) value);
        } else if (value instanceof String) {
            return with(placeholder, (String) value);
        } else if (value instanceof PlaceholderedMessage) {
            return with(placeholder, (PlaceholderedMessage) value);
        } else {
            throw new IllegalArgumentException("Unknown type: " + value.getClass().getName());
        }
    }

    @NotNull
    public Component build() {
        return MiniMessage.miniMessage().deserialize(message.getBase(), resolvers.toArray(new TagResolver[0]));
    }

    public void send(CommandSender sender) {
        sender.sendMessage(build());
    }

    public void broadcast() {
        Bukkit.broadcast(build());
    }
}
