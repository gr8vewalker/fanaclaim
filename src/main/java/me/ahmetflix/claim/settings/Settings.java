package me.ahmetflix.claim.settings;

import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Map;

public enum Settings {

    REFUND_CLAIM_BLOCKS(true),
    CUSTOM_WORLD_NAMES(Maps.newHashMap()),
    MAX_CLAIM_DAYS_BY_PERMISSION(Maps.newHashMap()),
    DAY_PRICE(1000);

    private final Object defaultValue;
    private Object value;

    Settings(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public boolean getBoolean() {
        return (boolean) value;
    }

    public Map getMap() {
        return (Map) value;
    }

    public int getInt() {
        return (int) value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static void load(ConfigurationSection settings) {
        Arrays.stream(values()).forEach(setting -> {
            Object defaultVal = setting.getDefaultValue();
            if (defaultVal instanceof Boolean) {
                setting.setValue(settings.getBoolean(setting.name(), (Boolean) defaultVal));
            } else if (defaultVal instanceof Map) {
                setting.setValue(settings.getConfigurationSection(setting.name()).getValues(false));
            }
        });
    }

}
