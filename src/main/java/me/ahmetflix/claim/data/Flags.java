package me.ahmetflix.claim.data;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.Arrays;

public class Flags {

    private final Object2BooleanOpenHashMap<Flag> flags = new Object2BooleanOpenHashMap<>();

    public Flags() {
        Arrays.stream(Flag.values())
                .forEach(flag -> flags.put(flag, false));
    }

    public boolean getFlag(Flag flag) {
        return flags.getBoolean(flag);
    }

    public void setFlag(Flag flag, boolean value) {
        flags.put(flag, value);
    }

    public boolean toggleFlag(Flag flag) {
        flags.put(flag, !flags.getBoolean(flag));
        return flags.getBoolean(flag);
    }

    public Object2BooleanOpenHashMap<Flag> getAllFlags() {
        return flags;
    }
}
