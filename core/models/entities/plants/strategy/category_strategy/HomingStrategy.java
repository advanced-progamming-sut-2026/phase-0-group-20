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
                        float dx = z.getX()/ PhysicalConstants.TILE_UNIT_LENGTH - plantCol;
                        float dy = (z.getRow() - plantRow) * PhysicalConstants.TILE_UNIT_LENGTH;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);
                        if (distance < minDistance) {
                            minDistance = distance;
                            target = z;
                        }
                    }
                } else {
                    target = validTargets.get(random.nextInt(validTargets.size()));
                }

                if (target != null) {
                    int burstCount = plantName.equals("Cat-tail") ? 2 : 1;
                    for (int i = 0; i < burstCount; i++)
                        ProjectileMechanism.executeTargetedProjectile(context, GameSession.getInstance(), target, i);
                    System.out.println(context.getName() + " locked onto " + target.getName() + "!");
                    lastShotTick = currentTick;

//                    shootHomingProjectile(context, target, gameSession);
//                    System.out.println(context.getName() + " locked onto " + target.getName() + "!");
//                    lastShotTick = currentTick;

                }
            }
        }
    }

//
//    private void shootHomingProjectile(Plant context, Zombie target, GameSession gameSession) {
//        float spawnX = context.getPlacedTile().getCol();
//        float spawnY = context.getPlacedTile().getRow();
//        String name = context.getName();
//
//        ProjectileEffect effect = null;
//        ProjectileType type = null;
//        int damage = 0;
//        int burstCount = 1;
//
//        if (name.equals("Caulipower")) {
//            effect = new HypnotizeEffect();
//            type = ProjectileType.MAGIC_BEAM;
//            damage = 0;
//        } else if (name.equals("Electric Blueberry")) {
//            effect = new LightningEffect();
//            type = ProjectileType.LIGHTNING_CLOUD;
//            damage = 5000;
//        } else if (name.equals("Cat-tail")) {
//            effect = new NormalEffect();
//            type = ProjectileType.PEA;
//            damage = 15;
//            burstCount = 2;
//        }
//
//        if (effect != null) {
//            for (int i = 0; i < burstCount; i++) {
//                int offset = -i;
//
//                Projectile projectile = new Projectile(
//                        context,
//                        type, effect, gameSession, damage,
//                        new Position(spawnX + offset, spawnY),
//                        0, 0,
//                        true, true
//                );
//
//                projectile.setHomingTarget(target, 1.5f);
//                gameSession.addProjectile(projectile);
//            }
//        }
//    }

}
