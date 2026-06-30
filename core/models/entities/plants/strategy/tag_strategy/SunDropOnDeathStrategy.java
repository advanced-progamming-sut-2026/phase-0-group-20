package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class SunDropOnDeathStrategy implements IPlantStrategy {
    private boolean hasDropped = false;

    @Override
    public void execute(Plant context, int currentTick) {

        if (context.getCurrentHp() <= 0 && !hasDropped) {
            System.out.println(context.getName() + " dropped sun on death!");
            hasDropped = true;
        }
    }
}
