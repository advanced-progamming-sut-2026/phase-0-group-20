package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTags;

import java.util.List;

public interface IPlant {

    int getId();

    String getName();

    PlantCategory getCategory();

    List<PlantTags> getTags();

    int getCost();

    int getBaseHp();

    int getDamage();

    String getBaseAbility();

}
