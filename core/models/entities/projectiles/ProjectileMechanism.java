package models.entities.projectiles;

import models.Position;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantCategory;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class ProjectileMechanism {


    public static void executeNewProjectile(Plant plant, boolean shootForward, boolean shootBackward) {
        int damage = plant.getDamage();
        ProjectileType type = getProjectileType(plant.getName());
        int plantRow = plant.getPlacedTile().getRow();
        int plantCol = plant.getPlacedTile().getCol();

        if (damage != -1 && type != null) {
            List<float[]> shotConfigs = getShotConfigurations(plant);

            for (float[] config : shotConfigs) {


                float spawnCol = plantCol + config[0];
                float spawnRow = plantRow + config[1];
                int speedX = (int) config[2];
                int speedY = (int) config[3];

                if (speedX > 0 && !shootForward) continue;
                if (speedX < 0 && !shootBackward) continue;
                if (spawnRow >= 0 && spawnRow < GameSession.getInstance().getArena().getRows()) {

                    Projectile.spawnNewProjectile(
                            plant,
                            type,
                            50,
                            new Position(spawnCol, spawnRow),
                            speedX,
                            speedY,
                            isPiercingProjectile(type),
                            canPassObstacles(plant)
                    );
                }
            }
        }
    }

    public static void executeTargetedProjectile(Plant plant, Zombie target, int burstIndex) {
        int damage = plant.getDamage();
        ProjectileType type = getProjectileType(plant.getName());

        ProjectileEffect effect = getProjectileEffect(plant.getName());

        if (damage != -1 && type != null) {
            float spawnCol = plant.getPlacedTile().getCol();
            float spawnRow = plant.getPlacedTile().getRow();
            float spawnX = spawnCol - burstIndex;

            Projectile projectile = Projectile.spawnNewProjectile(
                    plant,
                    type,
                    damage,
                    new Position(spawnX, spawnRow),
                    0,
                    0,
                    isPiercingProjectile(type),
                    canPassObstacles(plant)
            );

            projectile.setEffect(effect);
            projectile.setHomingTarget(target, 1.5f);
        }
    }

    public static ProjectileEffect getProjectileEffect(String name) {
        return switch (name) {
            case "Caulipower" -> new HypnotizeEffect();
            case "Electric Blueberry" -> new LightningEffect();
            default -> new NormalEffect();
        };
    }


    public static ProjectileType getProjectileType(String name) {
        return switch (name) {
            case "Snow Pea" -> ProjectileType.ICE_PEA;
            case "Rotobaga" -> ProjectileType.ROTOBAGA_SEED;
            case "Fire Peashooter" -> ProjectileType.FIRE_PEA;
            case "Goo Peashooter" -> ProjectileType.GOO_PEA;
            case "Caulipower" -> ProjectileType.MAGIC_BEAM;
            case "Electric Blueberry" -> ProjectileType.LIGHTNING_CLOUD;
            case "citron" -> ProjectileType.PLASMA_BALL;
            case "kernel-pult" -> ProjectileType.BUTTER;
            default -> ProjectileType.PEA;
        };
    }


    public static List<float[]> getShotConfigurations(Plant plant) {
        List<float[]> configs = new ArrayList<>(); // [offsetCol - offsetRow - speedX - speedY]
        String name = plant.getName();

        switch (name) {
            case "Peashooter":
            case "Snow Pea":
            case "Fire Peashooter":
            case "Goo Peashooter":
            case "Sea-shroom":
            case "Puff-shroom":
                configs.add(new float[]{0, 0, 1, 0});
                break;
            case "Repeater":
                configs.add(new float[]{0, 0, 1, 0});
                configs.add(new float[]{-1, 0, 1, 0});
                break;
            case "Pea Pod":
                for (int i = 0; i < plant.getStackCount(); i++) {
                    configs.add(new float[]{-i, 0, 1, 0});
                }
                break;
            case "Threepeater":
                configs.add(new float[]{0, -1, 1, 0}); // top line
                configs.add(new float[]{0, 0, 1, 0});  // middle line
                configs.add(new float[]{0, 1, 1, 0});  // bottom line
                break;
            case "Rotobaga":
                configs.add(new float[]{0, 0, 1, -1});  // top-right
                configs.add(new float[]{0, 0, 1, 1});   // bottom-right
                configs.add(new float[]{0, 0, -1, -1}); // top-left
                configs.add(new float[]{0, 0, -1, 1});  // bottom-left
                break;
            case "Split Pea":
                configs.add(new float[]{0, 0, 1, 0}); //forward
                configs.add(new float[]{0, 0, -1, 0}); // one backward
                configs.add(new float[]{1, 0, -1, 0}); // two backward
                break;
            case "Starfruit":
                configs.add(new float[]{0, 0, -1, 0});  // backward
                configs.add(new float[]{0, 0, 0, -1});  // up
                configs.add(new float[]{0, 0, 0, 1});   // down
                configs.add(new float[]{0, 0, 1, -1});  // up-right
                configs.add(new float[]{0, 0, 1, 1});   // down-right
                break;
            case "Mega Gatling Pea":
                configs.add(new float[]{0, 0, 1, 0});
                configs.add(new float[]{-1, 0, 1, 0});
                configs.add(new float[]{-2, 0, 1, 0});
                configs.add(new float[]{-3, 0, 1, 0});
                break;
        }
        return configs;
    }

    public static int parseDamage(String damage) {
        if (damage.matches("(-)?\\d+"))
            return Integer.parseInt(damage);
        return -1;
    }


    private static boolean isPiercingProjectile(ProjectileType type) {
        return type == ProjectileType.SPIKE;
    }

    private static boolean canPassObstacles(Plant plant) {
        return plant.getCategory() == PlantCategory.LOBBER;
    }


}
