package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.game.GameSession;

/**
 * Instantly produces a fixed amount of sun.
 * Used by: Sunflower (150), Twin Sunflower (250), Sun-shroom (225, also instant
 * grows to final stage), Primal Sunflower (225).
 */

public class SunBurstFoodStrategy implements PlantFoodStrategy {

    private final int sunAmount;

    public SunBurstFoodStrategy(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    @Override
    public void executeStrategy(Plant plant) {
        GameSession.getInstance().addSun(sunAmount);
        System.out.println(plant.getName() + " instantly produced " + sunAmount + " sun!");
    }
}
