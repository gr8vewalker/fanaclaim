package me.ahmetflix.claim;

import me.ahmetflix.claim.command.FanaClaimCommand;
import me.ahmetflix.claim.command.FanaClaimListCommand;
import me.ahmetflix.claim.command.ReloadCommand;
import me.ahmetflix.claim.data.Flag;
import me.ahmetflix.claim.gui.*;
import me.ahmetflix.claim.gui.item.ConfigItem;
import me.ahmetflix.claim.listener.ClaimCreateListener;
import me.ahmetflix.claim.listener.ClaimListener;
import me.ahmetflix.claim.listener.ClaimModifyListener;
import me.ahmetflix.claim.listener.InteractionListener;
import me.ahmetflix.claim.managers.ClaimManager;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.task.ClaimExpireRunnable;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FanaClaim extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(ConfigItem.class);
        ConfigurationSerialization.registerClass(ConfigItem.ItemInfo.class);
    }

    private static FanaClaim instance;
    private static DataStore griefPreventionDataStore;
    private static Economy economy;
    private static boolean skinsRestorer;

    public static FanaClaim getInstance() {
        return instance;
    }

    public static DataStore getGriefPreventionDataStore() {
        return griefPreventionDataStore;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isSkinsRestorer() {
        return skinsRestorer;
    }

    private ClaimManager claimManager;
    private ClaimMenu claimMenu;
    private ClaimListMenu claimListMenu;
    private ClaimRemoveMenu claimRemoveMenu;
    private ClaimExtendMenu claimExtendMenu;
    private ClaimSettingsMenu claimSettingsMenu;
    private ClaimSettingsPlayerListMenu claimSettingsPlayerListMenu;
    private ClaimSettingsPlayerMenu claimSettingsPlayerMenu;

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public ClaimMenu getClaimMenu() {
        return claimMenu;
    }

    public ClaimListMenu getClaimListMenu() {
        return claimListMenu;
    }

    public ClaimRemoveMenu getClaimRemoveMenu() {
        return claimRemoveMenu;
    }

    public ClaimExtendMenu getClaimExtendMenu() {
        return claimExtendMenu;
    }

    public ClaimSettingsMenu getClaimSettingsMenu() {
        return claimSettingsMenu;
    }

    public ClaimSettingsPlayerListMenu getClaimSettingsPlayerListMenu() {
        return claimSettingsPlayerListMenu;
    }

    public ClaimSettingsPlayerMenu getClaimSettingsPlayerMenu() {
        return claimSettingsPlayerMenu;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault/Ekonomi bulunamadı.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.saveDefaultConfig();
        this.reloadConfig();
        griefPreventionDataStore = GriefPrevention.instance.dataStore;
        claimManager = new ClaimManager();
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ClaimListener(), this);
        pluginManager.registerEvents(new InteractionListener(), this);
        pluginManager.registerEvents(new ClaimCreateListener(), this);
        pluginManager.registerEvents(new ClaimModifyListener(), this);
        pluginManager.registerEvents(new GuiListener(), this);
        long thirtyMinTicks = 30 * 60 * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> claimManager.save(), thirtyMinTicks, thirtyMinTicks);
        new ClaimExpireRunnable().runTaskTimerAsynchronously(this, 0L, 20L);
        getCommand("reloadfanaclaim").setExecutor(new ReloadCommand());
        getCommand("fanaclaim").setExecutor(new FanaClaimCommand());
        getCommand("fanaclaimlist").setExecutor(new FanaClaimListCommand());
        skinsRestorer = pluginManager.isPluginEnabled("SkinsRestorer");
    }

    @Override
    public void onDisable() {
        claimManager.save();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        FileConfiguration config = this.getConfig();
        Settings.load(config.getConfigurationSection("settings"));
        ConfigurationSection gui = config.getConfigurationSection("gui");
        ConfigurationSection guiClaimMenu = gui.getConfigurationSection("claim-menu");
        ConfigurationSection guiClaimListMenu = gui.getConfigurationSection("claim-list");
        ConfigurationSection guiClaimRemoveMenu = gui.getConfigurationSection("claim-remove");
        ConfigurationSection guiClaimExtendMenu = gui.getConfigurationSection("claim-time");
        ConfigurationSection guiClaimSettingsMenu = gui.getConfigurationSection("claim-config");
        ConfigurationSection guiClaimSettingsPlayerListMenu = gui.getConfigurationSection("claim-config-playerlist");
        ConfigurationSection guiClaimSettingsPlayerMenu = gui.getConfigurationSection("claim-config-player");
        claimMenu = new ClaimMenu(
                MiniMessage.miniMessage().deserialize(guiClaimMenu.getString("title", "Claim")),
                (List<ConfigItem>) guiClaimMenu.getList("OTHER"),
                guiClaimMenu.getObject("FILLER", ConfigItem.ItemInfo.class)
        );
        claimListMenu = new ClaimListMenu(
                MiniMessage.miniMessage().deserialize(guiClaimListMenu.getString("title", "Claim List")),
                guiClaimListMenu.getObject("PREVIOUS", ConfigItem.class),
                guiClaimListMenu.getObject("NEXT", ConfigItem.class),
                guiClaimListMenu.getObject("CLAIM", ConfigItem.ItemInfo.class)
        );
        claimRemoveMenu = new ClaimRemoveMenu(
                MiniMessage.miniMessage().deserialize(guiClaimRemoveMenu.getString("title", "Claim Remove")),
                guiClaimRemoveMenu.getObject("BACK", ConfigItem.class),
                guiClaimRemoveMenu.getObject("VERIFY_REMOVAL", ConfigItem.class),
                guiClaimRemoveMenu.getObject("FILLER", ConfigItem.ItemInfo.class)
        );
        claimExtendMenu = new ClaimExtendMenu(
                MiniMessage.miniMessage().deserialize(guiClaimExtendMenu.getString("title", "Claim Extend")),
                guiClaimExtendMenu.getObject("EXTEND_ADD_DAY", ConfigItem.class),
                guiClaimExtendMenu.getObject("EXTEND_REMOVE_DAY", ConfigItem.class),
                guiClaimExtendMenu.getObject("EXTEND_CONFIRM", ConfigItem.class),
                guiClaimExtendMenu.getObject("BACK", ConfigItem.class),
                guiClaimExtendMenu.getObject("FILLER", ConfigItem.ItemInfo.class)
        );
        claimSettingsMenu = new ClaimSettingsMenu(
                MiniMessage.miniMessage().deserialize(guiClaimSettingsMenu.getString("title", "Claim Settings")),
                guiClaimSettingsMenu.getObject("BACK", ConfigItem.class),
                guiClaimSettingsMenu.getObject("ANIMALS_ENABLED", ConfigItem.class),
                guiClaimSettingsMenu.getObject("ANIMALS_DISABLED", ConfigItem.class),
                guiClaimSettingsMenu.getObject("MONSTERS_ENABLED", ConfigItem.class),
                guiClaimSettingsMenu.getObject("MONSTERS_DISABLED", ConfigItem.class),
                guiClaimSettingsMenu.getObject("MEMBERS", ConfigItem.class),
                guiClaimSettingsMenu.getObject("FILLER", ConfigItem.ItemInfo.class)
        );
        claimSettingsPlayerListMenu = new ClaimSettingsPlayerListMenu(
                MiniMessage.miniMessage().deserialize(guiClaimSettingsPlayerListMenu.getString("title", "Claim Settings")),
                guiClaimSettingsPlayerListMenu.getObject("BACK", ConfigItem.class),
                guiClaimSettingsPlayerListMenu.getObject("PREVIOUS", ConfigItem.class),
                guiClaimSettingsPlayerListMenu.getObject("NEXT", ConfigItem.class),
                guiClaimSettingsPlayerListMenu.getObject("PLAYER", ConfigItem.ItemInfo.class)
        );
        Map<Flag, Integer> flagMap = new HashMap<>();
        guiClaimSettingsPlayerMenu.getConfigurationSection("SLOTS").getValues(false)
                .forEach((key, value) -> flagMap.put(Flag.valueOf(key), (Integer) value));
        claimSettingsPlayerMenu = new ClaimSettingsPlayerMenu(
                guiClaimSettingsPlayerMenu.getString("title", "Claim Settings"),
                guiClaimSettingsPlayerMenu.getObject("PLAYER", ConfigItem.class),
                guiClaimSettingsPlayerMenu.getObject("BACK", ConfigItem.class),
                guiClaimSettingsPlayerMenu.getObject("FLAG_ENABLED", ConfigItem.ItemInfo.class),
                guiClaimSettingsPlayerMenu.getObject("FLAG_DISABLED", ConfigItem.ItemInfo.class),
                guiClaimSettingsPlayerMenu.getObject("FILLER", ConfigItem.ItemInfo.class),
                flagMap
        );
        Messages.load();
    }
}
