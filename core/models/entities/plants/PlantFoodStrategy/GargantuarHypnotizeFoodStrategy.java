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
        plant.setBoosted(true);
        // we should implement it in collision logic that Hossein khahad zad
        // if the zombie eats a hypno-shroom plant that has been boosted, it will be destroyed
        // and new a gargantuar zombie will be born and hypnotized (working for plants)
        System.out.println(plant.getName() + " is now empowered: the next zombie to eat it will become a friendly Gargantuar!");
    }

    @Override
    public int getDurationTicks() {
        return -1;
    }

}
