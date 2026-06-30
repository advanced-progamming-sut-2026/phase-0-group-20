package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Global Effect Strategy:
 * Triggers an immediate, board-wide effect as soon as the plant is placed.
 * After the effect is applied, the plant typically disappears or dies shortly after.
 */

public class GlobalEffectStrategy implements IPlantStrategy {
    private boolean effectApplied = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (!effectApplied) {
            System.out.println(context.getName() + " activated a global board-wide effect!");

            // Logic to freeze all zombies, or buff all plants of a specific family

            effectApplied = true;
            context.takeDamage(context.getCurrentHp());
        }
    }
}
