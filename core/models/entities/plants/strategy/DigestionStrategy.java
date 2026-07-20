package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Digestion Strategy:
 * Used for plants like Chomper. The plant instantly kills (swallows) a zombie in range,
 * then enters a long "digestion" state where it becomes vulnerable and inactive.
 */

public class DigestionStrategy implements IPlantStrategy {
    private boolean isDigesting = false;
    private int digestionStartTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (isDigesting) {
            int currentDigestionTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
            if (currentTick - digestionStartTick >= currentDigestionTicks) {
                isDigesting = false;
                notify("🦷 " + context.getName() + " finished digesting and is hungry again!");
            }
        } else {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            Zombie target = null;
            double minDistance = Double.MAX_VALUE;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (z.isDead()) continue;

                double dist = z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol;

                if (dist >= -0.5 && dist <= 1.5) {
                    if (dist < minDistance) {
                        minDistance = dist;
                        target = z;
                    }
                }
            }

            if (target != null) {
                notify("🦖 " + context.getName() + " swallowed " + target.getName() + " whole!");
                boolean killed = target.takeDirectDamage(9999);
                if (killed) {
                    context.onZombieDeath(target);
                }

                isDigesting = true;
                digestionStartTick = currentTick;
            }
        }
    }
}
