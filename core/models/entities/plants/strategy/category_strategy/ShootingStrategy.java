package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.IceEffect;
import models.entities.projectiles.NormalEffect;
import models.entities.projectiles.Projectile;
import models.entities.projectiles.ProjectileEffect;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class ShootingStrategy implements IPlantStrategy {
    private int lastShotTick = 0;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            List<Integer> targetLines = projectileInLine(context.getName(), context.getPlacedTile().getRow());
            boolean shouldShoot = false;

            for (int line : targetLines) {
                if (!gameSession.zombieInRow(line).isEmpty()) {
                    shouldShoot = true;
                    break;
                }
            }

            if (shouldShoot) {
                int burstCount = getBurstCount(context.getName());
                for (int i = 0; i < burstCount; i++) {
                    executeNewProjectile(context, gameSession);
                }
                System.out.println(context.getName() + " fired projectiles!");
                lastShotTick = currentTick;
            }
        }
    }

    private void executeNewProjectile(Plant context, GameSession gameSession) {
        int damage = parseDamage(context.getDamage());
        ProjectileType type = getProjectileType(context.getName());

        if (damage != -1 && type != null) {
            List<Integer> targetLines = projectileInLine(context.getName(), context.getPlacedTile().getRow());

            for (int line : targetLines) {
                Projectile projectile = new Projectile(
                        type,
                        damage,
                        line,
                        context.getPlacedTile().getCol(),
                        false,
                        gameSession
                );

                projectile.addEffect(projectileEffect(projectile.getType()));
                gameSession.addProjectile(projectile);
            }
        }

    }

    private ProjectileType getProjectileType(String name) {
        switch (name) {
            case "Peashooter":
            case "Repeater":
            case "Threepeater":
                return ProjectileType.PEA;
            case "Snow Pea":
                return ProjectileType.ICE_PEA;
            default:
                return null;
        }
    }

    private List<Integer> projectileInLine(String name, int placedRow) {
        List<Integer> lines = new ArrayList<>();
        switch (name) {
            case "Peashooter":
            case "Repeater":
            case "Snow Pea":
                lines.add(placedRow);
                break;
            case "Threepeater":
                if (placedRow - 1 >= 0) lines.add(placedRow - 1);
                lines.add(placedRow);
                if (placedRow + 1 < 5) lines.add(placedRow + 1);
                break;
        }

        return lines;
    }

    private int parseDamage(String damage) {
        if (damage.matches("(-)?\\d+"))
            return Integer.parseInt(damage);
        return -1;
    }

    private ProjectileEffect projectileEffect(ProjectileType projectileType) {
        switch (projectileType) {
            case PEA:
                return new NormalEffect();
            case ICE_PEA:
                return new IceEffect();
            default:
                return null;
        }
    }

    private int getBurstCount(String name) {
        switch (name) {
            case "Repeater":
                return 2;
            default:
                return 1;
        }
    }
}
