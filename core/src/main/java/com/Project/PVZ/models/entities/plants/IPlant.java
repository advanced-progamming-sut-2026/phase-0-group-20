package com.Project.PVZ.models.entities.plants;

import com.Project.PVZ.models.enums.plants.PlantCategory;
import com.Project.PVZ.models.enums.plants.PlantTag;

import java.util.List;

public interface IPlant {
    int getId();

    String getName();

    PlantCategory getCategory();

    List<PlantTag> getTags();

    int getCost();

    int getBaseHp();

    int getDamage();

    String getAbilityType();

    float getAbilityValue();

    String getPlantFoodType();

    float getPlantFoodValue();

    float getActionInterval();

    float getRecharge();

}
