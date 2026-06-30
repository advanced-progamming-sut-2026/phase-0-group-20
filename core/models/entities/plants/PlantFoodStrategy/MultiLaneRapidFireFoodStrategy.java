package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Fires a fan-shaped rapid barrage across all of this plant's lanes at once.
 * Used by: Threepeater (3 lanes).
 */

public class MultiLaneRapidFireFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " fired a fan-shaped rapid barrage across all its lanes!");
    }
}
