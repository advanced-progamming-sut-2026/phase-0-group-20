package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * A single effect applied to every zombie on the field (or every zombie
 * currently visible on screen).
 * Used by: Garlic (force every zombie in the lane to move to another lane),
 * Kernel-pult (drop butter on every zombie's head, stunning them), Iceberg
 * Lettuce (freeze every zombie currently visible), Sweet Potato (pull in
 * every nearby zombie and fully heal itself).
 */

public class FieldWideEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public FieldWideEffectFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " triggered a field-wide effect: " + description);
    }
}
