package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

public interface PlantFoodStrategy {

    void executeStrategy(Plant plant);

    default boolean needsTimer() {
        return false;
    }
}
