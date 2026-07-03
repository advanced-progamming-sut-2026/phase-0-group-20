package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * A single powerful area-effect burst centered on (or just in front of) the
 * plant - melee smashes, smoke blasts, slams, etc.
 * Used by: Fume-shroom (giant smoke cloud that pushes zombies back),
 * Bonk Choy (rapid 3x3 punches), Phat Beet (powerful sonic 3x3 blast),
 * Wasabi Whip (spinning whip in 3x3), Kiwibeast (jump + ground slam AoE).
 */

public class BurstEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public BurstEffectFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " unleashed an area burst: " + description);
    }
}