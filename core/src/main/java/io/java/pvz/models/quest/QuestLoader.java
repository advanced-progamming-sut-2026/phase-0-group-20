package io.java.pvz.models.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class QuestLoader {

    public static List<Quest> loadQuestsFromJson(String filePath) {
        List<Quest> allQuests = new ArrayList<>();

        String resolvedPath = resolveResourcePath(filePath);

        try (Reader reader = new FileReader(resolvedPath)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                Quest quest = QuestFactory.buildQuest(jsonObject);

                if (quest != null) {
                    allQuests.add(quest);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading quests.json: " + e.getMessage());
        }

        return allQuests;
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
            File codeSource = new File(QuestLoader.class.getProtectionDomain()
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
}
