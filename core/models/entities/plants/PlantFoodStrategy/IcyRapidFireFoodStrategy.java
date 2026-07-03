package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Instantly freezes every zombie currently in the lane, then fires a rapid
 * barrage of icy projectiles down it.
 * Used by: Snow Pea.
 */

public class IcyRapidFireFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " froze the entire lane and unleashed an icy barrage!");
    }
}
