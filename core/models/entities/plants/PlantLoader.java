package models.entities.plants;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantLoader {

    public static List<PlantData> loadAll(String jsonPath) {
        List<PlantData> plants = new ArrayList<>();
        Gson gson = new Gson();

        try (Reader reader = new FileReader(jsonPath)) {
            Type listType = new TypeToken<ArrayList<PlantJsonDto>>() {
            }.getType();

            List<PlantJsonDto> dots = gson.fromJson(reader, listType);

            for (PlantJsonDto dto : dots) {
                PlantCategory category = parseCategory(dto.category);
                List<PlantTag> tags = parseTags(dto.tags);

                Map<Integer, PlantUpgrade> upgradesMap = new HashMap<>();
                if (dto.upgrades != null) {
                    for (UpgradeDto upgrade : dto.upgrades) {
                        UpgradeType type = UpgradeType.valueOf(upgrade.type);
                        upgradesMap.put(
                                upgrade.level,
                                new PlantUpgrade(type, upgrade.value, upgrade.specialTag)
                        );
                    }
                }

                plants.add(new PlantData(
                        dto.id,
                        dto.name,
                        category,
                        tags,
                        dto.cost,
                        dto.baseHp,
                        dto.damage,
                        dto.abilityType,
                        dto.abilityValue,
                        dto.plantFoodType,
                        dto.plantFoodValue,
                        upgradesMap,
                        dto.actionInterval,
                        dto.recharge
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
}
