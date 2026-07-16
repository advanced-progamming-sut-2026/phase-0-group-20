package models.entities.zombies.armour;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorLoader {

    private static ArmorLoader instance;
    private final Map<String, ArmorData> dataMap = new HashMap<>();

    private ArmorLoader(String jsonPath) {
        try {
            String raw = new String(Files.readAllBytes(Paths.get(jsonPath)));
            parseJson(raw);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load armors.json from: " + jsonPath, e);
        }
    }

    public static ArmorLoader getInstance() {
        if (instance == null) {
            instance = new ArmorLoader("core/resources/ArmorTypeData.json");
        }
        return instance;
    }

    public static void init(String jsonPath) {
        instance = new ArmorLoader(jsonPath);
    }

    private void parseJson(String raw) {
        JSONArray array = new JSONArray(raw);
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            JSONArray aliases = entry.getJSONArray("aliases");
            JSONObject od = entry.getJSONObject("objdata");

            String type = od.getString("ArmorType");
            int hp = od.getInt("BaseHealth");

            List<String> flagsList = new ArrayList<>();
            if (od.has("ArmorFlags")) {
                JSONArray flags = od.getJSONArray("ArmorFlags");
                for (int f = 0; f < flags.length(); f++) {
                    flagsList.add(flags.getString(f));
                }
            }

            ArmorData data = new ArmorData(aliases.getString(0), type, hp, flagsList);

            for (int j = 0; j < aliases.length(); j++) {
                dataMap.put(aliases.getString(j), data);
            }
        }
    }

    public ArmorData get(String alias) {
        if (alias.startsWith("RTID(") && alias.contains("@")) {
            alias = alias.substring(5, alias.indexOf("@"));
        }

        ArmorData d = dataMap.get(alias);
        if (d == null) throw new IllegalArgumentException("No armor data for alias: " + alias);
        return d;
    }

    public boolean has(String alias) {
        return dataMap.containsKey(alias);
    }
}
