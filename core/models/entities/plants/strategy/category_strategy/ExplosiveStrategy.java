package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

/**
 * Explosive Strategy:
 * Used for instant-kill plants like Cherry Bomb.
 * Triggers a massive explosion in a specific area shortly after being planted,
 * then instantly kills the plant itself.
 */

public class ExplosiveStrategy implements IPlantStrategy {

    private final int EXPLOSION_DELAY_TICKS = 10; // 1 sec delay for animation
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= EXPLOSION_DELAY_TICKS) {
            // Logic to deal massive damage to surrounding tiles
            System.out.println(context.getName() + " exploded, dealing massive damage!");

            // Kill the plant after explosion
            context.takeDamage(context.getCurrentHp());
        }
    }
}
