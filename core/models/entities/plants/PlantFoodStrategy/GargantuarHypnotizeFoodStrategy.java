package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Normally Hypno-shroom hypnotizes the single zombie that eats it. With
 * Plant Food, the next zombie that eats it is instead turned into a
 * fully-buffed Gargantuar fighting for the player.
 * Used by: Hypno-shroom.
 */

public class GargantuarHypnotizeFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " is now empowered: the next zombie to eat it will become a friendly Gargantuar!");
    }
}
