package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class StrikeThroughStrategy implements IPlantStrategy {
    private int lastShotTick = 0;
    private int rangeExtension = 0;
    private int pierceExtension = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean targetFound = false;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getCol() >= plantCol) {
                    if (context.getName().equals("Fume-shroom")) {
                        if (z.getCol() <= plantCol + 4 + rangeExtension) {
                            targetFound = true;
                            break;
                        }
                    } else {
                        targetFound = true;
                        break;
                    }
                }
            }

            if (!targetFound) {
                int obstacleCol = GameSession.getInstance().getArena().getFrontmostObstacleColInRow(plantRow, plantCol);
                if (obstacleCol != -1) {
                    if (context.getName().equals("Fume-shroom")) {
                        if (obstacleCol <= plantCol + 4 + rangeExtension) {
                            targetFound = true;
                        }
                    } else {
                        targetFound = true;
                    }
                }
            }

            if (targetFound) {
                context.triggerAction("attack");
                shootPiercingProjectile(context);
                lastShotTick = currentTick;
            }
        }
    }

    private void shootPiercingProjectile(Plant context) {
        String name = context.getName();
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        ProjectileType type = null;
        int damage = 0;
        int pierceLimit = 999;
        int lifespan = -1;

        if (name.equals("Cactus")) {
            type = ProjectileType.SPIKE;
            damage = context.getDamage();
            pierceLimit = 3 + pierceExtension;
        } else if (name.equals("Fume-shroom")) {
            type = ProjectileType.FUME;
            damage = context.getDamage();
            lifespan = 40;
        }

        if (type != null) {
            float speed = ProjectileTuning.speedFor(type);
            Projectile projectile = Projectile.spawnNewProjectile(
                context,
                type,
                damage,
                new Position(plantCol, plantRow),
                speed,
                0,
                true,
                false
            );

            projectile.setSpawnDelayTicks(0.5f);
            projectile.setPierceCount(pierceLimit);
            if (lifespan > 0) {
                projectile.setLifespanTicks(lifespan);
            }
            notify("💨 " + name + " fired a strike-through attack!");
        }
    }

    public void increaseRange(int range) {
        this.rangeExtension += range;
    }

    public void increasePierceLimit(int amount) {
        this.pierceExtension += amount;
    }
}
