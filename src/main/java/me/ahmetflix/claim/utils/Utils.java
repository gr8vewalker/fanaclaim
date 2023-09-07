package me.ahmetflix.claim.utils;

import me.ahmetflix.claim.message.Message;
import me.ahmetflix.claim.message.Messages;
import me.ahmetflix.claim.message.PlaceholderedMessage;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    public static void write(File file, String string) {
        try {
            Files.write(file.toPath(), string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
