package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Instantly arms the plant (skipping the normal arm-time delay) and throws
 * a number of cloned copies of itself onto other random tiles, already armed.
 * Used by: Potato Mine, Primal Potato Mine (2 clones each).
 */

public class CloneAndArmFoodStrategy implements PlantFoodStrategy {

    private final int cloneCount;

    public CloneAndArmFoodStrategy(int cloneCount) {
        this.cloneCount = cloneCount;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " instantly armed itself and threw " + cloneCount + " clone(s) onto the field!");
    }
}
