package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.*;
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
            boolean shootForward = false;
            boolean shootBackward = false;

            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            String plantName = context.getName();

            if (plantName.equals("Rotobaga")) {
                for (Zombie z : gameSession.getChosenZombies()) {
                    if (z.isDead()) continue;
                    int rowDiff = Math.abs(z.getRow() - plantRow);
                    int colDiff = (int) Math.abs(z.getX() - plantCol);
                    if (rowDiff == colDiff && rowDiff > 0 && rowDiff <= 2) {
                        shootForward = true;
                        shootBackward = true;
                        break;
                    }
                }
            } else if (plantName.equals("Starfruit")) {
                for (Zombie z : gameSession.getChosenZombies()) {
                    if (z.isDead()) continue;
                    int rowDiff = z.getRow() - plantRow;
                    int colDiff = (int) (z.getX() - plantCol);

                    boolean isBackward = (rowDiff == 0 && colDiff < 0);
                    boolean isUpOrDown = (colDiff == 0 && rowDiff != 0);
                    boolean isDiagonalForward = (colDiff > 0 && Math.abs(rowDiff) == colDiff);

                    if (isBackward || isUpOrDown || isDiagonalForward) {
                        shootForward = true;
                        shootBackward = true;
                        break;
                    }
                }
            } else {
                List<Integer> targetLines = projectileInLine(plantName, plantRow);
                for (int line : targetLines) {
                    for (Zombie z : gameSession.getArena().zombieInRow(line)) {
                        if (z.isDead()) continue;

                        int maxRange = (plantName.equals("Sea-shroom") || plantName.equals("Puff-shroom")) ? 3 : 999;

                        if (z.getX() >= plantCol && z.getX() <= plantCol + maxRange) shootForward = true;

                        if (z.getX() < plantCol) shootBackward = true;
                    }
                }
            }

            if (shootForward || shootBackward) {
                executeNewProjectile(context, gameSession, shootForward, shootBackward);
                System.out.println(plantName + " fired projectiles!");
                lastShotTick = currentTick;
            }
        }
    }

    private void executeNewProjectile(Plant context, GameSession gameSession, boolean shootForward, boolean shootBackward) {
        int damage = parseDamage(context.getDamage());
        ProjectileType type = getProjectileType(context.getName());
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        if (damage != -1 && type != null) {
            List<double[]> shotConfigs = getShotConfigurations(context);

            for (double[] config : shotConfigs) {
                double spawnCol = plantCol + config[0];
                double spawnRow = plantRow + config[1];
                double speedX = config[2];
                double speedY = config[3];

                if (speedX > 0 && !shootForward) continue;
                if (speedX < 0 && !shootBackward) continue;

                if (spawnRow >= 0 && spawnRow < gameSession.getArena().getRows()) {
                    Projectile projectile = new Projectile(
                            type,
                            projectileEffect(type),
                            gameSession,
                            damage,
                            spawnCol,
                            spawnRow,
                            speedX,
                            speedY,
                            false,
                            false
                    );
                    gameSession.addProjectile(projectile);
                }
            }
        }
    }

    private ProjectileType getProjectileType(String name) {
        return switch (name) {
            case "Snow Pea" -> ProjectileType.ICE_PEA;
            case "Rotobaga" -> ProjectileType.ROTOBAGA_SEED;
            case "Fire Peashooter" -> ProjectileType.FIRE_PEA;
            case "Goo Peashooter" -> ProjectileType.GOO_PEA;
            case "Peashooter", "Repeater", "Threepeater", "Pea Pod",
                 "Split Pea", "Starfruit", "Mega Gatling Pea",
                 "Sea-shroom", "Puff-shroom" -> ProjectileType.PEA;
            default -> null;
        };
    }

    private List<Integer> projectileInLine(String name, int placedRow) {
        List<Integer> lines = new ArrayList<>();
        lines.add(placedRow);
        if (name.equals("Threepeater")) {
            lines.add(placedRow - 1);
            lines.add(placedRow + 1);
        }
        return lines;
    }

    private List<double[]> getShotConfigurations(Plant context) {
        List<double[]> configs = new ArrayList<>(); // [offsetCol - offsetRow - speedX - speedY]
        String name = context.getName();

        switch (name) {
            case "Peashooter":
            case "Snow Pea":
            case "Fire Peashooter":
            case "Goo Peashooter":
            case "Sea-shroom":
            case "Puff-shroom":
                configs.add(new double[]{0, 0, 1, 0});
                break;
            case "Repeater":
                configs.add(new double[]{0, 0, 1, 0});
                configs.add(new double[]{-1, 0, 1, 0});
                break;
            case "Pea Pod":
                for (int i = 0; i < context.getStackCount(); i++) {
                    configs.add(new double[]{-i, 0, 1, 0});
                }
                break;
            case "Threepeater":
                configs.add(new double[]{0, -1, 1, 0}); // top line
                configs.add(new double[]{0, 0, 1, 0});  // middle line
                configs.add(new double[]{0, 1, 1, 0});  // bottom line
                break;
            case "Rotobaga":
                configs.add(new double[]{0, 0, 1, -1});  // top-right
                configs.add(new double[]{0, 0, 1, 1});   // bottom-right
                configs.add(new double[]{0, 0, -1, -1}); // top-left
                configs.add(new double[]{0, 0, -1, 1});  // bottom-left
                break;
            case "Split Pea":
                configs.add(new double[]{0, 0, 1, 0}); //forward
                configs.add(new double[]{0, 0, -1, 0}); // one backward
                configs.add(new double[]{1, 0, -1, 0}); // two backward
                break;
            case "Starfruit":
                configs.add(new double[]{0, 0, -1, 0});  // backward
                configs.add(new double[]{0, 0, 0, -1});  // up
                configs.add(new double[]{0, 0, 0, 1});   // down
                configs.add(new double[]{0, 0, 1, -1});  // up-right
                configs.add(new double[]{0, 0, 1, 1});   // down-right
                break;
            case "Mega Gatling Pea":
                configs.add(new double[]{0, 0, 1, 0});
                configs.add(new double[]{-1, 0, 1, 0});
                configs.add(new double[]{-2, 0, 1, 0});
                configs.add(new double[]{-3, 0, 1, 0});
                break;
        }
        return configs;
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
            case FIRE_PEA:
                return new FireEffect();
            case GOO_PEA:
                return new PoisonProjectileEffect();
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