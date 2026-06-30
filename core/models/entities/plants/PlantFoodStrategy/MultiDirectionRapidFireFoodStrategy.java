package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Fires a rapid barrage in several fixed directions at once.
 * Used by: Rotobaga (4 diagonal directions), Starfruit (5-point star, including backward).
 */

public class MultiDirectionRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int directionCount;

    public MultiDirectionRapidFireFoodStrategy(int directionCount) {
        this.directionCount = directionCount;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " fired a rapid barrage in all " + directionCount + " directions!");
    }
}
