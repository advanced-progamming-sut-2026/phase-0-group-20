package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.timeManager.TimeManager;

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
    private int aliveTicks = 0;

    public LifespanStrategy(int seconds) {
        this.lifespanTicks = seconds * TimeManager.TICKS_PER_SECOND;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        if (aliveTicks >= lifespanTicks) {
            context.takeDamage(context.getCurrentHp());
            notify(context.getName() + " vanished due to limited lifespan!");
        }
    }

    public void resetLifespan() {
        this.aliveTicks = 0;
    }
}