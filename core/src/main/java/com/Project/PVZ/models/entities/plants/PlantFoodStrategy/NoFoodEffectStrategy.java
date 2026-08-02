package com.Project.PVZ.models.entities.plants.PlantFoodStrategy;

import com.Project.PVZ.models.entities.plants.Plant;

public class NoFoodEffectStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        notify(plant.getName() + " has no Plant Food effect (one-shot/consumable plant).");
    }
}
