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
    private final int DIGESTION_DURATION_TICKS = 40 * TimeManager.TICKS_PER_SECOND;
    private boolean isDigesting = false;
    private int digestionStartTick = -1;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (isDigesting) {
            if (currentTick - digestionStartTick >= DIGESTION_DURATION_TICKS) {
                isDigesting = false;
                System.out.println("🦷 " + context.getName() + " finished digesting and is hungry again!");
            }
        } else {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            Zombie target = null;
            double minDistance = Double.MAX_VALUE;

            for (Zombie z : gameSession.getArena().zombieInRow(plantRow)) {
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
                System.out.println("🦖 " + context.getName() + " swallowed " + target.getName() + " whole!");
                target.takeDirectDamage(9999, context);

                isDigesting = true;
                digestionStartTick = currentTick;
            }
        }
    }
}
