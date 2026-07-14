package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

public class NoFoodEffectStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " has no Plant Food effect (one-shot/consumable plant).");
    }
}
