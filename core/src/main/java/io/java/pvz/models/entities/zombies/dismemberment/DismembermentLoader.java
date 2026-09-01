package io.java.pvz.models.entities.zombies.dismemberment;

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

public class DismembermentLoader {

    private static DismembermentLoader instance;
    private final Map<String, DismembermentData> dataMap = new HashMap<>();

    private DismembermentLoader(String jsonPath) {
        try {
            String raw = new String(Files.readAllBytes(Paths.get(jsonPath)));
            parseJson(raw);
        } catch (IOException e) {
            System.err.println("⚠️ Could not load ZombieDismembermentData.json from: " + jsonPath
                + " - the limb-loss effect will be disabled for every zombie. " + e.getMessage());
        }
    }

    public static DismembermentLoader getInstance() {
        if (instance == null) {
            String resolvedPath = resolveResourcePath("resources/ZombieDismembermentData.json");
            instance = new DismembermentLoader(resolvedPath);
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
            File codeSource = new File(DismembermentLoader.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            searchRoots.add(codeSource.getAbsoluteFile());
        } catch (Exception ignored) {
        }

        for (File root : searchRoots) {
            File dir = root.isFile() ? root.getParentFile() : root;
            for (int depth = 0; dir != null && depth < 6; depth++, dir = dir.getParentFile()) {
                File candidate = new File(dir, relativePath);
                if (candidate.isFile()) return candidate.getPath();

                File assetsCandidate = new File(dir, "assets/" + relativePath);
                if (assetsCandidate.isFile()) return assetsCandidate.getPath();
            }
        }
        return relativePath;
    }

    private void parseJson(String raw) {
        JSONArray array = new JSONArray(raw);
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.getJSONObject(i);
            String zombieTypeKey = entry.getString("zombieType");

            List<String> headParts = readStringArray(entry, "headParts");
            List<DismembermentData.ArmStage> armStages = readArmStages(entry);

            dataMap.put(zombieTypeKey, new DismembermentData(zombieTypeKey, headParts, armStages));
        }
    }

    private List<String> readStringArray(JSONObject entry, String key) {
        List<String> out = new ArrayList<>();
        if (entry.has(key)) {
            JSONArray arr = entry.getJSONArray(key);
            for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
        }
        return out;
    }

    private List<DismembermentData.ArmStage> readArmStages(JSONObject entry) {
        List<DismembermentData.ArmStage> stages = new ArrayList<>();
        if (!entry.has("armStages")) return stages;

        JSONArray arr = entry.getJSONArray("armStages");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject stageObj = arr.getJSONObject(i);
            List<String> parts = readStringArray(stageObj, "parts");
            int healthPercent = stageObj.has("healthPercent") ? stageObj.getInt("healthPercent") : 50;
            int chancePercent = stageObj.has("chancePercent") ? stageObj.getInt("chancePercent") : 100;
            stages.add(new DismembermentData.ArmStage(parts, healthPercent, chancePercent));
        }
        return stages;
    }

    public DismembermentData get(String zombieTypeKey) {
        return dataMap.get(zombieTypeKey);
    }

    public boolean has(String zombieTypeKey) {
        return dataMap.containsKey(zombieTypeKey);
    }
}
