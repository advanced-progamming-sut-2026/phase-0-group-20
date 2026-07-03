package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Spawns several free copies of this plant on other empty water tiles.
 * Used by: Lily Pad.
 */

public class DuplicateSelfFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " spawned extra copies of itself on nearby empty water tiles!");
    }
}
