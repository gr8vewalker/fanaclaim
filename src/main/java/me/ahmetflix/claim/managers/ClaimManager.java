package me.ahmetflix.claim.managers;

import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.ahmetflix.claim.FanaClaim;
import me.ahmetflix.claim.data.ClaimData;
import me.ahmetflix.claim.settings.Settings;
import me.ahmetflix.claim.utils.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ClaimManager {

    private final Long2ObjectMap<ClaimData> claims = new Long2ObjectOpenHashMap<>();

    public ClaimManager() {
        load();
    }

    private void load() {
        File folder = new File(FanaClaim.getInstance().getDataFolder(), "claims");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                if (name.endsWith(".json")) {
                    try {
                        ClaimData data = ClaimData.fromJson(JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject());
                        if (FanaClaim.getGriefPreventionDataStore().getClaim(data.getId()) == null) {
                            path.toFile().delete();
                            return;
                        }
                        claims.put(data.getId(), data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        claims.values().forEach(ClaimData::save);
    }

    public ClaimData addClaim(long claimId) {
        return claims.computeIfAbsent(claimId, ClaimData::new);
    }

    public void deleteClaim(Claim claim) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(FanaClaim.getInstance(), () -> deleteClaim(claim));
            return;
        }
        removeData(claim);
        FanaClaim.getGriefPreventionDataStore().deleteClaim(claim);
    }

    public void removeData(Claim claim) {
        if (!Settings.REFUND_CLAIM_BLOCKS.getBoolean()) {
            PlayerData playerData = FanaClaim.getGriefPreventionDataStore().getPlayerData(claim.ownerID);
            playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() - claim.getArea());
        }
        ClaimData claimData = claims.remove(claim.getID().longValue());
        if (claimData != null) {
            claimData.closeInventories();
            claimData.delete();
        }
    }

    public ClaimData getClaim(long claimId) {
        return claims.get(claimId);
    }

    public Collection<ClaimData> getClaims() {
        return claims.values();
    }
}
