package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Lifespan Strategy:
 * Used for plants with a limited lifetime (Puff-shroom, Sea-shroom: 60s base).
 * Once the lifespan expires, the plant dies automatically.
 * Plant Food resets the lifespan back to full (and, for Sea-shroom, resets
 * every Sea-shroom on the board - that part must be handled by the
 * PlantFoodStrategy/GlobalEffectStrategy layer, not here).
 */

public class LifespanStrategy implements IPlantStrategy {
    private final int lifespanTicks;
    private int startTick = -1;

    public LifespanStrategy(int lifespanSeconds) {
        this.lifespanTicks = lifespanSeconds * 10;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) {
            startTick = currentTick;
            return;
        }

        if (currentTick - startTick >= lifespanTicks) {
            System.out.println(context.getName() + "'s lifespan expired and it wilted away.");
            context.takeDamage(context.getCurrentHp());
        }
    }

    public void resetLifespan(int currentTick) {
        this.startTick = currentTick;
    }
}