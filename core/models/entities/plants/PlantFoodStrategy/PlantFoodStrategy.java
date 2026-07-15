package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

public interface PlantFoodStrategy {

    void executeStrategy(Plant plant);

    default int getDurationTicks() {
        return 0;
    }

    default void reset() {
    }
}
