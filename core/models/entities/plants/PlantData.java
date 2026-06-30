package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.util.List;

public record PlantData(
        int id,
        String name,
        PlantCategory category,
        List<PlantTag> tags,
        int cost,
        int baseHp,
        String damage,
        String baseAbility,
        String plantFoodEffect,
        String lvl2,
        String lvl3,
        String lvl4,
        float actionInterval,
        int recharge
) {}
