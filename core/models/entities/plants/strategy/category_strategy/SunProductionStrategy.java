package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class SunProductionStrategy implements IPlantStrategy {
    private int lastProductionTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastProductionTick) >= intervalInTicks) {
            System.out.println(context.getName() + " produced sun!");
            // logic for add sun

            lastProductionTick = currentTick;
        }
    }
}
