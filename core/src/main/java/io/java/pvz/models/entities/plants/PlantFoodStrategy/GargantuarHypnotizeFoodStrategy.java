package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;

/**
 * Normally Hypno-shroom hypnotizes the single zombie that eats it. With
 * Plant Food, the next zombie that eats it is instead turned into a
 * fully-buffed Gargantuar fighting for the player.
 * Used by: Hypno-shroom.
 */

public class GargantuarHypnotizeFoodStrategy implements PlantFoodStrategy {
    private boolean executed = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            plant.setBoosted(true);
            executed = true;
        }
    }

    @Override
    public int getDurationTicks() {
        return -1;
    }

    @Override
    public void reset() {
        this.executed = false;
    }
}
