package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.NormalEffect;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

/**
 * Explosive Strategy:
 * Used for instant-kill plants like Cherry Bomb.
 * Triggers a massive explosion in a specific area shortly after being planted,
 * then instantly kills the plant itself.
 */

public class ExplosiveStrategy implements IPlantStrategy {

    private final int EXPLOSION_DELAY_TICKS = 1 * TimeManager.TICKS_PER_SECOND; // 1 sec delay for animation
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= EXPLOSION_DELAY_TICKS) {
            String name = context.getName();
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();

            int damage = 1800;

            System.out.println("💥 " + name + " DETONATED!");

            switch (name) {
                case "Cherry Bomb":
                case "Grapeshot":
                    applyAreaDamage(gameSession, plantCol, plantRow, 1.5, damage);

                    if (name.equals("Grapeshot")) {
                        double[][] directions = {
                                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                                {0.707, 0.707}, {-0.707, 0.707}, {0.707, -0.707}, {-0.707, -0.707}
                        };

                        for (double[] dir : directions) {
                            Projectile grape = new Projectile(
                                    ProjectileType.GRAPE, new NormalEffect(), gameSession, 60,
                                    plantCol, plantRow,
                                    dir[0] * 2.5, dir[1] * 2.5,
                                    false, false
                            );

                            grape.setLifespanTicks(5 * TimeManager.TICKS_PER_SECOND);
                            grape.setBouncesLeft(20);

                            gameSession.addProjectile(grape);
                        }
                        System.out.println("🍇 Grapeshot scattered 8 bouncing grapes in all directions!");
                    }
                    break;

                case "Jalapeno":
                    for (Zombie z : gameSession.zombieInRow(plantRow)) {
                        if (!z.isDead()) {
                            z.removeChillEffect();
                            z.takeDirectDamage(damage);
                        }
                    }
                    System.out.println("🔥 Jalapeno burned the entire lane!");
                    break;

                case "Doom-shroom":
                    applyAreaDamage(gameSession, plantCol, plantRow, 3.5, damage);
                    // change tile type
                    System.out.println("🕳️ Doom-shroom left a massive crater behind!");
                    break;
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    private void applyAreaDamage(GameSession gameSession, int col, int row, double radius, int damage) {
        List<Zombie> targets = gameSession.getArena().getZombiesInRadius(col, row, radius);
        for (Zombie z : targets) {
            if (!z.isDead()) {
                z.takeDirectDamage(damage);
            }
        }
    }
}
