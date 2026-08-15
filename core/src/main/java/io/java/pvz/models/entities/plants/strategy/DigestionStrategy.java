package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

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

                double dist = (z.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH - plantCol;

                if (dist >= -0.5 && dist <= 1.5) {
                    if (dist < minDistance) {
                        minDistance = dist;
                        target = z;
                    }
                }
            }

            if (target != null) {
                context.triggerAction("bite");

                notify("🦖 " + context.getName() + " swallowed " + target.getName() + " whole!");
                target.takeDamage(9999);
                if (target.isDead()) {
                    context.onZombieDeath(target);
                }

                isDigesting = true;
                digestionStartTick = currentTick;
            }
        }
    }

    public boolean isDigesting() {
        return isDigesting;
    }
}
