package me.ahmetflix.claim.data;

import me.ahmetflix.claim.message.Message;
import me.ahmetflix.claim.message.Messages;

public enum Flag {

    BUILD(Messages.FLAG_BUILD),
    BUILD_HOPPERS(Messages.FLAG_BUILD_HOPPERS),
    BREAK_BEACON(Messages.FLAG_BREAK_BEACON),
    BREAK_SPAWNER(Messages.FLAG_BREAK_SPAWNER),
    PLACE_SPAWNER(Messages.FLAG_PLACE_SPAWNER),
    PLACE_FLUID(Messages.FLAG_PLACE_FLUID),
    PLACE_ENTITIES(Messages.FLAG_PLACE_ENTITIES),
    USE_ROD_ON_ENTITIES(Messages.FLAG_USE_ROD_ON_ENTITIES),
    USE_DOORS(Messages.FLAG_USE_DOORS),
    USE_SETHOME(Messages.FLAG_USE_SETHOME),
    OPEN_CONTAINERS(Messages.FLAG_OPEN_CONTAINERS),
    TRIGGER_REDSTONE(Messages.FLAG_TRIGGER_REDSTONE);

    private final Message message;

    Flag(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
