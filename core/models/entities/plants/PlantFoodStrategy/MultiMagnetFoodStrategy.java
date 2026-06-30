package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Instantly pulls metallic items off of every zombie in range at once,
 * instead of the normal one-at-a-time, on-cooldown behavior.
 * Used by: Magnet-shroom.
 */

public class MultiMagnetFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " yanked every metallic item in range off zombies at once!");
    }
}
