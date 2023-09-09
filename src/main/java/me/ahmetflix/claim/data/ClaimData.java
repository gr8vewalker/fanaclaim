package me.ahmetflix.claim.data;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.gui.MenuType;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public class ClaimData {

    private final long id;
    private boolean animals;
    private boolean mobs;
    private long end;
    private int area;
    private final Object2ObjectOpenHashMap<UUID, Flags> flags = new Object2ObjectOpenHashMap<>();

    private final ObjectOpenHashSet<UUID> guis = new ObjectOpenHashSet<>();

    public ClaimData(long id) {
        this(id, true, true, System.currentTimeMillis() + (Settings.DEFAULT_DAYS.getInt() * 86400000L), new Object2ObjectOpenHashMap<>());
        save();
    }

    public ClaimData(long id, boolean animals, boolean mobs, long end, Object2ObjectOpenHashMap<UUID, Flags> flags) {
        this.id = id;
        this.animals = animals;
        this.mobs = mobs;
        this.end = end;
        this.flags.putAll(flags);
    }

    public long getId() {
        return id;
    }

    public boolean isAnimals() {
        return animals;
    }

    public boolean isMobs() {
        return mobs;
    }

    public long getEnd() {
        return end;
    }

    public int getArea() {
        if (area == 0) {
            Claim claim = getGriefPreventionClaim();
            this.modify((int) Utils.calculateArea(claim));
        }
        return area;
    }

    public Flags getFlags(UUID uuid) {
        return flags.computeIfAbsent(uuid, uid -> new Flags());
    }

    public void setAnimals(boolean animals) {
        this.animals = animals;
    }

    public void setMobs(boolean mobs) {
        this.mobs = mobs;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void renew(double daysToAdd) {
        this.end += (long) (daysToAdd * 86400000L);
        Bukkit.getScheduler().runTaskAsynchronously(FanaClaim.getInstance(), this::save);
    }

    public void subtract(double daysToSubtract) {
        this.end -= (long) (daysToSubtract * 86400000L);
        Bukkit.getScheduler().runTaskAsynchronously(FanaClaim.getInstance(), this::save);
    }

    public void modify(int area) {
        if (area > this.area) {
            this.area = area;
            Bukkit.getScheduler().runTaskAsynchronously(FanaClaim.getInstance(), this::save);
        }
    }

    public void openMenu(Player player, MenuType type, Object... args) {
        guis.add(player.getUniqueId());
        switch (type) {
            case MAIN_CLAIM:
                FanaClaim.getInstance().getClaimMenu().open(this, player);
                break;
            case CLAIM_LIST:
                FanaClaim.getInstance().getClaimListMenu().open(player);
                break;
            case CLAIM_REMOVE:
                FanaClaim.getInstance().getClaimRemoveMenu().open(this, player);
                break;
            case ADD_TIME:
                FanaClaim.getInstance().getClaimExtendMenu().open(this, player);
                break;
            case CLAIM_SETTINGS:
                FanaClaim.getInstance().getClaimSettingsMenu().open(this, player);
                break;
            case CLAIM_SETTINGS_PLAYERLIST:
                FanaClaim.getInstance().getClaimSettingsPlayerListMenu().open(this, player);
                break;
            case CLAIM_SETTINGS_PLAYER:
                FanaClaim.getInstance().getClaimSettingsPlayerMenu().open(this, player, (OfflinePlayer) args[0]);
                break;
        }
    }

    public void removeMenu(UUID uuid) {
        this.guis.remove(uuid);
    }

    public void closeInventories() {
        this.guis.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(HumanEntity::closeInventory);
    }

    public void save() {
        File dir = new File(FanaClaim.getInstance().getDataFolder(), "claims");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, id + ".json");
        Utils.write(file, toJson().toString());
    }

    public void delete() {
        File dir = new File(FanaClaim.getInstance().getDataFolder(), "claims");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, id + ".json");
        if (file.exists()) file.delete();
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("animals", animals);
        object.addProperty("mobs", mobs);
        object.addProperty("end", end);
        object.addProperty("area", area);
        JsonObject flagsObject = new JsonObject();
        flags.forEach((uuid, flag) -> {
            JsonObject flagObject = new JsonObject();
            flag.getAllFlags().forEach((flag1, value) -> flagObject.addProperty(flag1.name(), value));
            flagsObject.add(uuid.toString(), flagObject);
        });
        object.add("flags", flagsObject);
        return object;
    }

    public Claim getGriefPreventionClaim() {
        return FanaClaim.getGriefPreventionDataStore().getClaim(id);
    }

    public TagResolver[] createTagResolvers() {
        Claim claim = getGriefPreventionClaim();
        Location middleCorner = Utils.middleCornerLocation(claim);
        return new TagResolver[] {
                TagResolver.resolver("size", Tag.preProcessParsed(String.valueOf((int)Utils.calculateArea(claim)))),
                TagResolver.resolver("x", Tag.preProcessParsed(String.valueOf(middleCorner.getX()))),
                TagResolver.resolver("y", Tag.preProcessParsed(String.valueOf(middleCorner.getY()))),
                TagResolver.resolver("z", Tag.preProcessParsed(String.valueOf(middleCorner.getZ()))),
                TagResolver.resolver("world", Tag.preProcessParsed((String) Settings.CUSTOM_WORLD_NAMES.getMap().get(claim.getLesserBoundaryCorner().getWorld().getName()))),
                Placeholder.component("remaining", Utils.convertToMessage(end - System.currentTimeMillis()).build())
        };
    }

    public static ClaimData fromJson(JsonObject object) {
        long id = object.get("id").getAsLong();
        boolean animals = object.get("animals").getAsBoolean();
        boolean mobs = object.get("mobs").getAsBoolean();
        long end = object.get("end").getAsLong();
        int area = object.get("area").getAsInt();
        Object2ObjectOpenHashMap<UUID, Flags> flags = new Object2ObjectOpenHashMap<>();
        JsonObject flagsObject = object.getAsJsonObject("flags");
        flagsObject.entrySet().forEach(entry -> {
            UUID uuid = UUID.fromString(entry.getKey());
            Flags flag = new Flags();
            JsonObject flagObject = entry.getValue().getAsJsonObject();
            flagObject.entrySet().forEach(flagEntry -> flag.setFlag(Flag.valueOf(flagEntry.getKey()), flagEntry.getValue().getAsBoolean()));
            flags.put(uuid, flag);
        });
        ClaimData claimData = new ClaimData(id, animals, mobs, end, flags);
        claimData.modify(area);
        return claimData;
    }

}
