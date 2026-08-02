package com.Project.PVZ.models.entities.plants;

import com.Project.PVZ.models.enums.plants.PlantCategory;
import com.Project.PVZ.models.enums.plants.PlantTag;

import java.util.List;
import java.util.Map;

public record PlantData(
        int id,
        String name,
        PlantCategory category,
        List<PlantTag> tags,
        int cost,
        int baseHp,
        int damage,
        String abilityType,
        float abilityValue,
        String plantFoodType,
        float plantFoodValue,
        Map<Integer, PlantUpgrade> upgrades,
        float actionInterval,
        float recharge
) {
}
