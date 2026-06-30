package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Fires a rapid barrage simultaneously forward and backward.
 * Used by: Split Pea.
 */

public class BidirectionalRapidFireFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " fired a rapid barrage forward AND backward!");
    }
}