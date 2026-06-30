package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

public class LobberStrategy implements IPlantStrategy {
    private int lastLobTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastLobTick) >= intervalInTicks) {
            System.out.println(context.getName() + " lobbed a projectile over obstacles!");
            // logic for produce a projectile

            lastLobTick = currentTick;
        }
    }
}
