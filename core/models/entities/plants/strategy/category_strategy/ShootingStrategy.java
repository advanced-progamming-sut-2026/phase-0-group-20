package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class ShootingStrategy implements IPlantStrategy {
    private int lastShotTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            // check if zombie exist in this line to shoot a new projectile

            System.out.println(context.getName() + " fired a pea/projectile!");

            lastShotTick = currentTick;
        }
    }
}
