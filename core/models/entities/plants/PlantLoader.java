package models.entities.plants;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlantLoader {

    private static class PlantJsonDto {
        int id;
        String name;
        String category;
        List<String> tags;
        int cost;
        int baseHp;
        int damage;
        float actionInterval;
        float recharge;
        String abilityType;
        float abilityValue;
        String plantFoodType;
        float plantFoodValue;
        List<UpgradeDto> upgrades;
    }

    private static class UpgradeDto {
        int level;
        String type;
        float value;
        String specialTag;
    }



    public static List<PlantData> loadAll(String jsonPath) {
        List<PlantData> plants = new ArrayList<>();
        Gson gson = new Gson();

        try (Reader reader = new FileReader(jsonPath)) {
            Type listType = new TypeToken<ArrayList<PlantJsonDto>>(){}.getType();

            List<PlantJsonDto> dots = gson.fromJson(reader, listType);

            for (PlantJsonDto dto : dots) {
                PlantCategory category = parseCategory(dto.category);
                List<PlantTag> tags = parseTags(dto.tags);

                String damageStr = String.valueOf(dto.damage);

                String baseAbility = dto.abilityType + (dto.abilityValue != 0 ? ":" + dto.abilityValue : "");
                String plantFoodEffect = dto.plantFoodType + (dto.plantFoodValue != 0 ? ":" + dto.plantFoodValue : "");

                String lvl2 = "-", lvl3 = "-", lvl4 = "-";
                if (dto.upgrades != null) {
                    for (UpgradeDto upgrade : dto.upgrades) {
                        String upgradeStr = upgrade.type + ":" + upgrade.value +
                                (upgrade.specialTag != null && !upgrade.specialTag.isEmpty() ? ":" + upgrade.specialTag : "");
                        switch (upgrade.level) {
                            case 2 -> lvl2 = upgradeStr;
                            case 3 -> lvl3 = upgradeStr;
                            case 4 -> lvl4 = upgradeStr;
                        }
                    }
                }

                plants.add(new PlantData(
                        dto.id,
                        dto.name,
                        category,
                        tags,
                        dto.cost,
                        dto.baseHp,
                        damageStr,
                        baseAbility,
                        plantFoodEffect,
                        lvl2,
                        lvl3,
                        lvl4,
                        dto.actionInterval,
                        (int) dto.recharge
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading plants from JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return plants;
    }

    private static PlantCategory parseCategory(String categoryStr) {
        try {
            if (categoryStr == null || categoryStr.isEmpty()) return null;
            return PlantCategory.valueOf(categoryStr.toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<PlantTag> parseTags(List<String> tagsList) {
        List<PlantTag> tags = new ArrayList<>();
        if (tagsList == null || tagsList.isEmpty()) return tags;

        for (String t : tagsList) {
            try {
                tags.add(PlantTag.valueOf(normalizeTag(t.trim())));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return tags;
    }

    private static String normalizeTag(String raw) {
        String withUnderscores = raw.replaceAll("([a-z])([A-Z])", "$1_$2");
        return withUnderscores.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}
