package me.ahmetflix.claim.created;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.ahmetflix.claim.FanaClaim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class PlayersClaimCreated {

    private final ObjectArrayList<UUID> created = new ObjectArrayList<>();
    private final File file = new File(FanaClaim.getInstance().getDataFolder(), "created.json");

    public void init() {
        if (!file.exists()) save();

        try {
            JsonArray array = JsonParser.parseReader(Files.newBufferedReader(file.toPath())).getAsJsonArray();
            array.forEach(element -> created.add(UUID.fromString(element.getAsString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        JsonArray array = new JsonArray();
        created.forEach(uuid -> array.add(uuid.toString()));
        try {
            Files.write(file.toPath(), array.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasCreatedBefore(UUID uuid) {
        return created.contains(uuid);
    }

    public void addCreated(UUID uuid) {
        if (created.contains(uuid)) return;
        created.add(uuid);
    }

}
