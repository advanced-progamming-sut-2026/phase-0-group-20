package models.entities.zombies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZombieLoader {

    private static ZombieLoader instance;
    private final Map<String, ZombieData> dataMap = new HashMap<>();

    private ZombieLoader(String jsonPath) {
        try {
            String raw = new String(Files.readAllBytes(Paths.get(jsonPath)));
            parseJson(raw);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load zombies.json from: " + jsonPath, e);
        }
    }

    public static ZombieLoader getInstance() {
        if (instance == null) {
            // default
            instance = new ZombieLoader("assets/zombies.json");
        }
        return instance;
    }

    public static void init(String jsonPath) {
        instance = new ZombieLoader(jsonPath);
    }

    private void parseJson(String raw) {
        JSONArray array = new JSONArray(raw);
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            JSONArray aliases = entry.getJSONArray("aliases");
            JSONObject od = entry.getJSONObject("objdata");

            int hp = od.optInt("Hitpoints", 190);
            float speed = (float) od.optDouble("Speed", 0.185);
            int eatDps = od.optInt("EatDPS", 100);
            int waveCost = od.optInt("WavePointCost", 100);
            boolean food = od.optBoolean("CanSpawnPlantFood", true);
            int smash = od.optInt("SmashDamage", 0);
            String imp = od.optString("ImpType", "");

            List<String> armorProps = new ArrayList<>();
            if (od.has("ZombieArmorProps")) {
                JSONArray armors = od.getJSONArray("ZombieArmorProps");
                for (int a = 0; a < armors.length(); a++) {
                    armorProps.add(armors.getString(a));
                }
            }

            ZombieData data = new ZombieData(aliases.getString(
                    0), hp, speed, eatDps, waveCost, food, smash, imp, armorProps
            );

            for (int j = 0; j < aliases.length(); j++) {
                dataMap.put(aliases.getString(j), data);
            }
        }
    }

    public ZombieData get(String alias) {
        ZombieData d = dataMap.get(alias);
        if (d == null) throw new IllegalArgumentException("No zombie data for alias: " + alias);
        return d;
    }

    public ZombieData get(ZombieType type) {
        return get(type.getJsonAlias());
    }

    public boolean has(String alias) {
        return dataMap.containsKey(alias);
    }
}
