package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.util.List;

public interface IPlant {
    int getId();
    String getName();
    PlantCategory getCategory();
    List<PlantTag> getTags();
    int getCost();
    int getBaseHp();
    String getDamage();
    String getBaseAbility();
    String getPlantFoodEffect();
    String getLvl2();
    String getLvl3();
    String getLvl4();
    float getActionInterval();
    int getRecharge();

}
