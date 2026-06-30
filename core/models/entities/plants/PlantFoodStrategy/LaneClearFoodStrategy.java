package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Unleashes one powerful, lane-clearing attack that pierces/damages every
 * zombie in the lane.
 * Used by: Citron (plasma ball that clears the whole lane), Cactus (a burst
 * of high-damage, infinite-pierce electrified spikes).
 */

public class LaneClearFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public LaneClearFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " unleashed a lane-clearing attack: " + description);
    }
}
