package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Fires a rapid barrage AND resets the lifespan timer of every plant of the
 * same species currently on the board (they have a limited 60s lifespan -
 * see LifespanStrategy).
 * Used by: Sea-shroom, Puff-shroom.
 */

public class RapidFireResetLifespanFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " fired a rapid barrage and reset the lifespan of all "
                + plant.getName() + "s on the board!");
    }
}
