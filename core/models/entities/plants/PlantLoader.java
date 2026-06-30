package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlantLoader {

    public static List<PlantData> loadAll(String csvPath) {
        List<PlantData> plants = new ArrayList<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] rawData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String[] data = new String[14];
                for (int i = 0; i < rawData.length; i++) {
                    data[i] = rawData[i].replaceAll("^\"|\"$", "").trim();
                }

                int id = Integer.parseInt(data[0]);
                String name = data[1];
                PlantCategory category = parseCategory(data[2]);
                List<PlantTag> tags = parseTags(data[3]);
                int cost = Integer.parseInt(data[4]);
                int baseHp = Integer.parseInt(data[5]);
                String damage = data[6];
                String baseAbility = data[7];
                String plantFoodEffect = data[8];
                String lvl2 = data[9];
                String lvl3 = data[10];
                String lvl4 = data[11];

                float actionInterval = 0;
                if (!data[12].equals("-") && !data[12].isEmpty()) {
                    try {
                        actionInterval = Float.parseFloat(data[12]);
                    } catch (NumberFormatException ignored) {}
                }

                int recharge = Integer.parseInt(data[13]);

                plants.add(new PlantData(id, name, category, tags, cost, baseHp, damage, baseAbility,
                        plantFoodEffect, lvl2, lvl3, lvl4, actionInterval, recharge));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return plants;
    }

    private static PlantCategory parseCategory(String categoryStr) {
        try {
            return PlantCategory.valueOf(categoryStr.toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<PlantTag> parseTags(String tagsStr) {
        List<PlantTag> tags = new ArrayList<>();
        if (tagsStr.equals("-") || tagsStr.isEmpty()) return tags;

        String[] tagArray = tagsStr.split(",");
        for (String t : tagArray) {
            try {
                tags.add(PlantTag.valueOf(normalizeTag(t.trim())));
            } catch (IllegalArgumentException ignored) {}
        }
        return tags;
    }

    private static String normalizeTag(String raw) {
        String withUnderscores = raw.replaceAll("([a-z])([A-Z])", "$1_$2");
        return withUnderscores.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}
