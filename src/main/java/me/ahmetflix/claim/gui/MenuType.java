package me.ahmetflix.claim.gui;

import java.util.Arrays;

public enum MenuType {

    MAIN_CLAIM(""),
    ADD_TIME("claim-time"),
    CLAIM_LIST("claim-list"),
    CLAIM_REMOVE("claim-remove"),
    CLAIM_SETTINGS("claim-config"),
    CLAIM_SETTINGS_PLAYERLIST("claim-config-playerlist"),
    CLAIM_SETTINGS_PLAYER("claim-config-player"),
    ;

    private final String id;

    MenuType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static MenuType getById(String id) {
        return Arrays.stream(values())
                .filter(type -> type.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

}
