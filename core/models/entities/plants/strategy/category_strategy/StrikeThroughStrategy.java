package models.entities.plants.strategy.category_strategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.NormalEffect;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Strike-Through Strategy:
 * Generates a projectile or laser beam that does not disappear upon hitting the first target.
 * It penetrates and deals damage to all enemies in a single line or specific range.
 */

public class StrikeThroughStrategy implements IPlantStrategy {
    private int lastShotTick = 0;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean zombieFound = false;

            for (Zombie z : gameSession.getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getCol() >= plantCol) {
                    if (context.getName().equals("Fume-shroom")) {
                        if (z.getCol() <= plantCol + 4) {
                            zombieFound = true;
                            break;
                        }
                    } else {
                        zombieFound = true;
                        break;
                    }
                }
            }

            if (zombieFound) {
                shootPiercingProjectile(context, gameSession);
                lastShotTick = currentTick;
            }
        }
    }

    private void shootPiercingProjectile(Plant context, GameSession gameSession) {
        String name = context.getName();
        float spawnX = context.getPlacedTile().getCol();
        float spawnY = context.getPlacedTile().getRow();

        ProjectileType type = null;
        int damage = 0;
        int pierceLimit = 999;
        int lifespan = -1;

        if (name.equals("Cactus")) {
            type = ProjectileType.SPIKE;
            damage = 30;
            pierceLimit = 3;
        } else if (name.equals("Fume-shroom")) {
            type = ProjectileType.FUME;
            damage = 20;
            lifespan = 40;
        }

        if (type != null) {
            Projectile projectile = new Projectile(
                    context,
                    type,
                    new NormalEffect(),
                    gameSession,
                    damage,
                    new Position(spawnX, spawnY),
                    0.1f,
                    0,
                    true,
                    false
            );

            projectile.setPierceCount(pierceLimit);
            if (lifespan > 0) {
                projectile.setLifespanTicks(lifespan);
            }

            gameSession.addProjectile(projectile);
            System.out.println("💨 " + name + " fired a strike-through attack!");
        }
    }
}
