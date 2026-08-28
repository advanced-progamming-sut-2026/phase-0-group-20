package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class HomingStrategy implements IPlantStrategy {
    public enum TargetMode {NEAREST, RANDOM, GARGANTUAR_FIRST}
    private final Random random = new Random();
    private int lastShotTick = 0;
    private TargetMode targetMode;
    private int burstCount;

    private int pendingBurstShots = 0;
    private int burstCooldownTicks = 0;
    private Zombie pendingTarget;
    private boolean targetingObstacle = false;

    public HomingStrategy(TargetMode targetMode, int burstCount) {
        this.targetMode = targetMode;
        this.burstCount = burstCount;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        if (handlePendingBurst(context)) {
            return;
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            processNewShot(context, currentTick);
        }
    }

    private boolean handlePendingBurst(Plant context) {
        if (pendingBurstShots <= 0) {
            return false;
        }

        if (burstCooldownTicks > 0) {
            burstCooldownTicks--;
        } else {
            fireBurstShot(context);
            updateBurstState();
        }
        return true;
    }

    private void fireBurstShot(Plant context) {
        if (pendingTarget != null && !pendingTarget.isDead()) {
            context.triggerAction("attack");
            ProjectileMechanism.executeTargetedProjectile(context, pendingTarget, 0.5f);
        } else if (targetingObstacle) {
            context.triggerAction("attack");
            ProjectileMechanism.executeNewProjectile(context, true, false, 0.5f);
        }
    }

    private void updateBurstState() {
        pendingBurstShots--;
        burstCooldownTicks = ProjectileTuning.VOLLEY_STAGGER_TICKS;

        if (pendingBurstShots <= 0) {
            targetingObstacle = false;
        }
    }

    private void processNewShot(Plant context, int currentTick) {
        List<Zombie> activeZombies = GameSession.getInstance().getArena().getActiveZombies();
        List<Zombie> validTargets = activeZombies.stream().filter(z -> !z.isDead()).toList();

        if (!validTargets.isEmpty()) {
            attemptZombieTargeting(context, currentTick, validTargets);
        } else {
            attemptObstacleTargeting(context, currentTick);
        }
    }

    private void attemptZombieTargeting(Plant context, int currentTick, List<Zombie> validTargets) {
        Zombie target = selectTarget(context, validTargets);

        if (target != null && target.getCol() < GameSession.getInstance().getArena().getCols()) {
            context.triggerAction("attack");
            ProjectileMechanism.executeTargetedProjectile(context, target, 0.5f);
            notify(context.getName() + " locked onto " + target.getName() + "!");

            setupBurstMode(target, false);
            lastShotTick = currentTick;
        }
    }

    private void attemptObstacleTargeting(Plant context, int currentTick) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();
        int obstacleCol = GameSession.getInstance().getArena()
            .getFrontmostObstacleColInRow(plantRow, plantCol);

        if (obstacleCol != -1) {
            context.triggerAction("attack");
            ProjectileMechanism.executeNewProjectile(context, true, false, 0.5f);
            notify(context.getName() + " fired at an obstacle!");

            setupBurstMode(null, true);
            lastShotTick = currentTick;
        }
    }

    private void setupBurstMode(Zombie target, boolean isObstacle) {
        if (burstCount > 1) {
            pendingBurstShots = burstCount - 1;
            burstCooldownTicks = ProjectileTuning.VOLLEY_STAGGER_TICKS;
            pendingTarget = target;
            targetingObstacle = isObstacle;
        }
    }

    private Zombie selectTarget(Plant context, List<Zombie> validTargets) {
        if (validTargets.isEmpty()) return null;
        switch (targetMode) {
            case NEAREST -> {
                float minDistance = Float.MAX_VALUE;
                int plantRow = context.getPlacedTile().getRow();
                int plantCol = context.getPlacedTile().getCol();
                Zombie nearest = null;

                for (Zombie z : validTargets) {
                    float dx = (z.getX() - PhysicalConstants.GRID_START_X / PhysicalConstants.TILE_WIDTH) - plantCol;
                    float dy = z.getRow() - plantRow;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = z;
                    }
                }
                return nearest;
            }
            case GARGANTUAR_FIRST -> {
                List<Zombie> gargantuars = validTargets.stream()
                    .filter(z -> z.getName().toLowerCase().contains("gargantuar")).toList();
                if (!gargantuars.isEmpty()) return gargantuars.get(random.nextInt(gargantuars.size()));
                return validTargets.get(random.nextInt(validTargets.size()));
            }
            case RANDOM -> {return validTargets.get(random.nextInt(validTargets.size()));}
            default -> {return validTargets.get(0);}
        }
    }

    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
    }

    public void setBurstCount(int burstCount) {
        this.burstCount = burstCount;
    }
}
