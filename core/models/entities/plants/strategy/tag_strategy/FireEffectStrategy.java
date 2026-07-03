package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class FireEffectStrategy implements IPlantStrategy {
    private final int WARM_UP_INTERVAL_TICKS = 5;
    private int lastWarmUpTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        if (currentTick - lastWarmUpTick >= WARM_UP_INTERVAL_TICKS) {

            // Logic to warm up adjacent tiles (3x3 grid around the plant)

            System.out.println(context.getName() + " is radiating heat to adjacent tiles!");

            lastWarmUpTick = currentTick;
        }
    }
}
