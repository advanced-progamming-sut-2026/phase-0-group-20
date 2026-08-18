package io.java.pvz.models.entities.zombies.armour;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
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
            String resolvedPath = resolveResourcePath("resources/ArmorTypeData.json");
            instance = new ArmorLoader(resolvedPath);
        }
        return instance;
    }

    private static String resolveResourcePath(String relativePath) {
        String override = System.getProperty("pvz.resourcesDir");
        if (override != null) {
            File candidate = new File(override, relativePath);
            if (candidate.isFile()) return candidate.getPath();
        }

        File direct = new File(relativePath);
        if (direct.isFile()) return relativePath;

        List<File> searchRoots = new ArrayList<>();
        searchRoots.add(new File(".").getAbsoluteFile());
        try {
            File codeSource = new File(ArmorLoader.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            searchRoots.add(codeSource.getAbsoluteFile());
        } catch (Exception ignored) {

        }

        List<String> tried = new ArrayList<>();
        for (File root : searchRoots) {
            File dir = root.isFile() ? root.getParentFile() : root;
            for (int depth = 0; dir != null && depth < 6; depth++, dir = dir.getParentFile()) {
                File candidate = new File(dir, relativePath);
                tried.add(candidate.getPath());
                if (candidate.isFile()) return candidate.getPath();

                File assetsCandidate = new File(dir, "assets/" + relativePath);
                tried.add(assetsCandidate.getPath());
                if (assetsCandidate.isFile()) return assetsCandidate.getPath();
            }
        }

        throw new RuntimeException("Could not find '" + relativePath + "'. Tried:\n  "
            + String.join("\n  ", tried)
            + "\nRun with -Dpvz.resourcesDir=<path to the folder containing 'resources/'> "
            + "or launch the process with the project root as its working directory.");
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

            List<String> armorLayers = new ArrayList<>();
            if (od.has("ArmorLayers")) {
                JSONArray layers = od.getJSONArray("ArmorLayers");
                for (int f = 0; f < layers.length(); f++) {
                    armorLayers.add(layers.getString(f));
                }
            }

            String layerGroup = od.has("ArmorLayerGroup") ? od.getString("ArmorLayerGroup") : null;
            ArmorData data = new ArmorData(aliases.getString(0), type, hp, flagsList, armorLayers, layerGroup);

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
