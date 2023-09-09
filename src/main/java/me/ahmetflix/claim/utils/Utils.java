package me.ahmetflix.claim.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.message.PlaceholderedMessage;
import me.ahmetflix.claim.settings.Settings;
import me.ryanhamshire.GriefPrevention.Claim;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.property.IProperty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static double calculateArea(Claim claim) {
        Location min = claim.getLesserBoundaryCorner();
        Location max = claim.getGreaterBoundaryCorner();
        return Math.abs((max.getX() - min.getX()) * (max.getZ() - min.getZ()));
    }

    public static PlaceholderedMessage convertToMessage(long ms) {
        long daysLong = ms / 86400000L;
        long hoursLong = ms / 3600000L % 24L;
        long minutesLong = ms / 60000L % 60L;
        long secondsLong = ms / 1000L % 60L;

        return Messages.FULL_TIME_FORMAT
                .with("days", daysLong == 0 ? "" : Messages.DAYS.with("days", String.valueOf(daysLong)))
                .with("hours", hoursLong == 0 ? "" : Messages.HOURS.with("hours", String.valueOf(hoursLong)))
                .with("minutes", minutesLong == 0 ? "" : Messages.MINUTES.with("minutes", String.valueOf(minutesLong)))
                .with("seconds", Messages.SECONDS.with("seconds", String.valueOf(secondsLong)));
    }

    public static Player getDamager(Entity entity) {
        if (entity instanceof Player) return (Player) entity;
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Player) return (Player) shooter;
        }
        return null;
    }

    public static Location middleCornerLocation(Claim claim) {
        return new Location(claim.getLesserBoundaryCorner().getWorld(), (claim.getLesserBoundaryCorner().getX() + claim.getGreaterBoundaryCorner().getX()) / 2.0, (claim.getLesserBoundaryCorner().getY() + claim.getGreaterBoundaryCorner().getY()) / 2.0, (claim.getLesserBoundaryCorner().getZ() + claim.getGreaterBoundaryCorner().getZ()) / 2.0);
    }

    public static void teleportSafeLocation(Player player, Location location) {
        player.teleport(location.getWorld().getHighestBlockAt(location).getLocation().add(0.5, 1.0, 0.5));
    }

    public static List<UUID> getTrustedPlayers(Claim claim) {
        List<UUID> trustedPlayers = new ObjectArrayList<>();
        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();
        claim.getPermissions(builders, containers, accessors, managers);
        permissionsToPlayers(trustedPlayers, builders);
        permissionsToPlayers(trustedPlayers, containers);
        permissionsToPlayers(trustedPlayers, accessors);
        permissionsToPlayers(trustedPlayers, managers);
        return trustedPlayers;
    }

    private static void permissionsToPlayers(List<UUID> set, List<String> perms) {
        perms.stream()
                .filter(s -> !s.startsWith("[") && !s.equals("public"))
                .map(UUID::fromString)
                .filter(uuid -> !set.contains(uuid))
                .forEach(set::add);
    }

    public static void setSkullOwner(SkullMeta meta, OfflinePlayer player) {
        if (!FanaClaim.isSkinsRestorer()) {
            meta.setOwningPlayer(player);
            return;
        }
        String skin = SkinsRestorerAPI.getApi().getSkinName(player.getName());
        if (skin == null) {
            meta.setOwningPlayer(player);
            return;
        }
        IProperty property = SkinsRestorerAPI.getApi().getSkinData(skin);
        if (property == null) {
            meta.setOwningPlayer(player);
            return;
        }
        PlayerProfile profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
        profile.clearProperties();
        profile.getProperties().removeIf(profileProperty -> profileProperty.getName().equals(IProperty.TEXTURES_NAME));
        profile.getProperties().add(new ProfileProperty(IProperty.TEXTURES_NAME, property.getValue(), property.getSignature()));
        meta.setPlayerProfile(profile);
    }

    public static void write(File file, String string) {
        try {
            Files.write(file.toPath(), string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Pair<String, Integer>> generatePermissions() {
        List<Pair<String, Integer>> claimDaysPermissions = new ObjectArrayList<>();
        Settings.MAX_CLAIM_DAYS_BY_PERMISSION.getMap().forEach((key, value) -> {
            String skey = (String) key;
            if (skey.equalsIgnoreCase("default")) return;
            if (value instanceof MemorySection) {
                claimDaysPermissions.addAll(getPermissions(skey, (MemorySection) value));
            } else {
                claimDaysPermissions.add(Pair.of(skey, (Integer) value));
            }
        });
        claimDaysPermissions.sort((o1, o2) -> o2.value() - o1.value());
        return claimDaysPermissions;
    }

    private static List<Pair<String, Integer>> getPermissions(String key, MemorySection value) {
        List<Pair<String, Integer>> permissions = new ObjectArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        value.getKeys(false).forEach(s -> {
            String newKey = key + "." + s;
            Object o = value.get(s);
            if (o instanceof MemorySection) {
                permissions.addAll(getPermissions(newKey, (MemorySection) o));
            } else {
                permissions.add(Pair.of(newKey, (Integer) o));
            }
            counter.getAndIncrement();
        });
        if (counter.get() == 0) {
            permissions.add(Pair.of(key, (Integer) value.get(key)));
        }
        return permissions;
    }
}
