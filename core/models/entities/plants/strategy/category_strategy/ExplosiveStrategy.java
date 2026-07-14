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

import java.util.List;

/**
 * Explosive Strategy:
 * Used for instant-kill plants like Cherry Bomb.
 * Triggers a massive explosion in a specific area shortly after being planted,
 * then instantly kills the plant itself.
 */

public class ExplosiveStrategy implements IPlantStrategy {

    private static final int EXPLOSION_DELAY_TICKS = TimeManager.TICKS_PER_SECOND; // 1 sec delay for animation
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
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
                    applyAreaDamage(plantCol, plantRow, 1.5f, damage,context);

                    if (name.equals("Grapeshot")) {
                        float[][] directions = {
                                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                                {0.707f, 0.707f}, {-0.707f, 0.707f}, {0.707f, -0.707f}, {-0.707f, -0.707f}
                        };

                        for (float[] dir : directions) {
                            Projectile grape = new Projectile(
                                    context,
                                    ProjectileType.GRAPE, new NormalEffect(),60,
                                    new Position(plantCol, plantRow),
                                    dir[0] * 2.5f, dir[1] * 2.5f,
                                    false, false
                            );

                            grape.setLifespanTicks(5 * TimeManager.TICKS_PER_SECOND);
                            grape.setBouncesLeft(20);

                            GameSession.getInstance().getArena().addProjectile(grape);
                        }
                        System.out.println("🍇 Grapeshot scattered 8 bouncing grapes in all directions!");
                    }
                    break;

                case "Jalapeno":
                    for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                        if (!z.isDead()) {
                            z.removeChillEffect();
                            z.removeFreezeEffect();
                            z.takeDirectDamage(damage,context);
                        }
                    }
                    System.out.println("🔥 Jalapeno burned the entire lane!");
                    break;

                case "Doom-shroom":
                    applyAreaDamage(plantCol, plantRow, 3.5f, damage,context);
                    // change tile type
                    System.out.println("🕳️ Doom-shroom left a massive crater behind!");
                    break;
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    private void applyAreaDamage(int col, int row, float radius, int damage,Plant plant) {
        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(col, row, radius);
        for (Zombie z : targets) {
            if (!z.isDead()) {
                z.takeDirectDamage(damage,plant);
            }
        }
    }
}
