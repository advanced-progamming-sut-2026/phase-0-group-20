package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class WrampUpStrategy implements IPlantStrategy {
    private int startTick = -1;
    private int growthStage = 1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        int elapsedTicks = currentTick - startTick;

        if (growthStage == 1 && elapsedTicks >= (24 * 10)) {
            growthStage = 2;
            System.out.println(context.getName() + " grew to stage 2!");

        } else if (growthStage == 2 && elapsedTicks >= (72 * 10)) {
            growthStage = 3;
            System.out.println(context.getName() + " grew to stage 3!");
        }
    }
}
