package models.entities.plants.strategy.category_strategy;


import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.ProjectileMechanism;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

/**
 * Homing Strategy:
 * This DigestionStrategy scans the entire board (or specific lanes) to find the nearest zombie.
 * Instead of shooting straight, it locks onto a target and fires a homing projectile.
 */

public class HomingStrategy implements IPlantStrategy {
    private final Random random = new Random();
    private int lastShotTick = 0;
    private boolean prioritizeGargantuars = false;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            List<Zombie> activeZombies = GameSession.getInstance().getArena().getActiveZombies();
            List<Zombie> validTargets = activeZombies.stream().filter(z -> !z.isDead()).toList();

            if (!validTargets.isEmpty()) {
                String plantName = context.getName();
                Zombie target = null;

                if (plantName.equals("Cat-tail")) {
                    float minDistance = Float.MAX_VALUE;
                    int plantRow = context.getPlacedTile().getRow();
                    int plantCol = context.getPlacedTile().getCol();

                    for (Zombie z : validTargets) {
                        float dx = z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol;
                        float dy = (z.getRow() - plantRow) * PhysicalConstants.TILE_UNIT_LENGTH;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);
                        if (distance < minDistance) {
                            minDistance = distance;
                            target = z;
                        }
                    }
                } else {
                    if (prioritizeGargantuars) {
                        List<Zombie> gargantuars = validTargets.stream()
                                .filter(z -> z.getName().toLowerCase().contains("gargantuar"))
                                .toList();

                        if (!gargantuars.isEmpty()) {
                            target = gargantuars.get(random.nextInt(gargantuars.size()));
                        }
                    }

                    if (target == null) {
                        target = validTargets.get(random.nextInt(validTargets.size()));
                    }
                }
                if (target != null) {
                    int burstCount = plantName.equals("Cat-tail") ? 2 : 1;
                    for (int i = 0; i < burstCount; i++)
                        ProjectileMechanism.executeTargetedProjectile(context, target, i);
                    notify(context.getName() + " locked onto " + target.getName() + "!");
                    lastShotTick = currentTick;
                }
            }
        }
    }

    public void setPrioritizeGargantuars(boolean prioritize) {
        this.prioritizeGargantuars = prioritize;
    }

}
