package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class MeleeStrategy implements IPlantStrategy {
    private int lastAttackTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastAttackTick) >= intervalInTicks) {
            // check collision
            System.out.println(context.getName() + " dealt melee damage!");

            lastAttackTick = currentTick;
        }
    }
}
