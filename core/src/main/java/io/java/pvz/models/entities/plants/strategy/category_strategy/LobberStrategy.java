package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.projectiles.*;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class LobberStrategy implements IPlantStrategy {
    private int lastLobTick = 0;
    private int splashDamageBonus = 0;
    private float paperRadiusBonus = 0;

    private float butterChanceBonus = 0.0f;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastLobTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            float plantCol = context.getPlacedTile().getCol();

            Zombie targetZombie = null;
            float minDistance = Float.MAX_VALUE;
            boolean canShootBackward = context.getTags().contains(PlantTag.AOE);

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead()) {
                    if (z.getCol() < plantCol && !canShootBackward) continue;
                    float distance = Math.abs(z.getCol() - plantCol);
                    if (distance < minDistance && z.getCol() < 12) {
                        minDistance = distance;
                        targetZombie = z;
                    }
                }
            }

            int obstacleCol = -1;
            if (targetZombie == null) {
                obstacleCol = GameSession.getInstance().getArena()
                    .getFrontmostObstacleColInRow(plantRow, (int) plantCol);
            }

            if (targetZombie != null || obstacleCol != -1) {
                executeNewLobbedProjectile(context, targetZombie, obstacleCol);
                lastLobTick = currentTick;
            }
        }
    }


    private void executeNewLobbedProjectile(Plant context, Zombie targetZombie, int obstacleCol) {
        Projectile projectile = createBaseLobbedProjectile(context);

        if (projectile != null) {
            setProjectileTrajectory(projectile, targetZombie, obstacleCol, context.getPlacedTile().getRow());

            projectile.setSpawnDelayTicks(0.5f);
            Projectile.spawnCustom(projectile);

            triggerLobAnimation(context, projectile.getType());
        }
    }

    private Projectile createBaseLobbedProjectile(Plant context) {
        String name = context.getName();
        int damage = context.getDamage();
        int spawnX = context.getPlacedTile().getCol() - 1;
        int spawnY = context.getPlacedTile().getRow();

        ProjectileType type = null;
        ProjectileEffect effect = new NormalEffect();

        switch (name) {
            case "Cabbage-pult":
                type = ProjectileType.CABBAGE;
                break;
            case "Kernel-pult":
                if (Math.random() < 0.25 + butterChanceBonus) {
                    type = ProjectileType.BUTTER;
                    effect = new ButterEffect();
                } else {
                    type = ProjectileType.CORN;
                }
                break;
            case "Melon-pult":
                type = ProjectileType.MELON;
                effect = new SplashEffect(damage + splashDamageBonus);
                break;
            case "Winter Melon":
                type = ProjectileType.WINTER_MELON;
                effect = new IceSplashEffect(damage + splashDamageBonus);
                break;
            case "Pepper-pult":
                type = ProjectileType.PEPPER;
                effect = new FireSplashEffect(paperRadiusBonus + 1.5);
                break;
        }

        if (type == null) return null;

        return new Projectile(
            context, type, effect, damage,
            new Position(spawnX, spawnY),
            0, 0, false, true
        );
    }

    private void setProjectileTrajectory(Projectile projectile, Zombie target, int obstacleCol, int spawnY) {
        float speed = ProjectileTuning.LOB_SPEED_TILES_PER_SEC;
        float height = ProjectileTuning.LOB_ARC_HEIGHT_TILES;

        if (target != null) {
            projectile.setArcTrajectory(target, speed, height);
        } else if (obstacleCol != -1) {
            projectile.setArcTrajectory(new Position(obstacleCol, spawnY), speed, height);
        }
    }

    private void triggerLobAnimation(Plant context, ProjectileType type) {
        String name = context.getName();

        if (name.equals("Kernel-pult") && type == ProjectileType.BUTTER) {
            context.triggerAction("attack2");
        } else {
            context.triggerAction("attack");
        }

        notify("🥔 " + name + " lobbed a " + type.name() + "!");
    }

    public void increaseSplashDamage(int damage) {
        this.splashDamageBonus += damage;
    }

    public void increaseWarmRadius(float r) {
        this.paperRadiusBonus += r;
    }

    public void increaseButterChance(float chance) {
        this.butterChanceBonus += chance;
    }
}
