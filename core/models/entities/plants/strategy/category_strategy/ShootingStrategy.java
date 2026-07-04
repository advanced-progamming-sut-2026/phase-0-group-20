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
            boolean shouldShoot = false;
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();

            if (context.getName().equals("Rotobaga")) {
                for (Zombie z : gameSession.getChosenZombies()) {
                    int rowDiff = Math.abs(z.getRow() - plantRow);
//                    int colDiff = Math.abs(z.getX() - plantCol);

//                    if (rowDiff == colDiff && rowDiff > 0) {
//                        shouldShoot = true;
//                        break;
//                    }
                }
            } else {
                List<Integer> targetLines = projectileInLine(context.getName(), context.getPlacedTile().getRow());
                for (int line : targetLines) {
                    if (!gameSession.zombieInRow(line).isEmpty()) {
                        shouldShoot = true;
                        break;
                    }
                }
            }


            if (shouldShoot) {
                int burstCount = getBurstCount(context);
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
            List<int[]> directions = getShootDirections(context.getName());

            for (int[] dir : directions) {
                int dirX = dir[0];
                int dirY = dir[1];

                Projectile projectile = new Projectile(
                        type,
                        projectileEffect(type),
                        gameSession,
                        damage,
                        context.getPlacedTile().getRow(),
                        context.getPlacedTile().getCol(),
                        dirX,
                        dirY,
                        false,
                        false
                );

                gameSession.addProjectile(projectile);
            }
        }

    }

    private ProjectileType getProjectileType(String name) {
        switch (name) {
            case "Peashooter":
            case "Repeater":
            case "Threepeater":
            case "Pea Pod":
                return ProjectileType.PEA;
            case "Snow Pea":
                return ProjectileType.ICE_PEA;
            case "Rotobaga":
                return ProjectileType.ROTOBAGA_SEED;
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
            case "Pea Pod":
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

    private List<int[]> getShootDirections(String name) {
        List<int[]> directions = new ArrayList<>();
        switch (name) {
            case "Peashooter":
            case "Repeater":
            case "Threepeater":
            case "Snow Pea":
            case "Pea Pod":
                directions.add(new int[]{1, 0});
                break;
            case "Rotobaga":
                directions.add(new int[]{1, -1});  // up-right
                directions.add(new int[]{1, 1});   // bottom-right
                directions.add(new int[]{-1, -1}); // up-left
                directions.add(new int[]{-1, 1}); // bottom-left
                break;
        }
        return directions;
    }

    private int parseDamage(String damage) {
        if (damage.matches("(-)?\\d+"))
            return Integer.parseInt(damage);
        return -1;
    }

    private ProjectileEffect projectileEffect(ProjectileType projectileType) {
        switch (projectileType) {
            case PEA:
            case ROTOBAGA_SEED:
                return new NormalEffect();
            case ICE_PEA:
                return new IceEffect();
            default:
                return null;
        }
    }

    private int getBurstCount(Plant context) {
        switch (context.getName()) {
            case "Rotobaga":
                return 3;
            case "Repeater":
                return 2;
            case "Pea Pod":
                return context.getStackCount();
            default:
                return 1;
        }
    }
}