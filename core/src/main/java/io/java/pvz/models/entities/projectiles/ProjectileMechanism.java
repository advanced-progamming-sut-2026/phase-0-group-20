package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class ProjectileMechanism {


    public static void executeNewProjectile(Plant plant, boolean shootForward, boolean shootBackward, float delaySeconds) {
        int damage = plant.getDamage();
        ProjectileType type = getProjectileType(plant.getName());
        int plantRow = plant.getPlacedTile().getRow();
        int plantCol = plant.getPlacedTile().getCol();

        if (damage == -1 || type == null) return;

        float speed = ProjectileTuning.speedFor(type);
        List<float[]> shotConfigs = getShotConfigurations(plant);

        for (float[] config : shotConfigs) {
            int spawnCol = plantCol + (int) config[0];
            int spawnRow = plantRow + (int) config[1];
            float speedX = config[2] * speed;
            float speedY = config[3] * speed;

            if (speedX > 0 && !shootForward) continue;
            if (speedX < 0 && !shootBackward) continue;
            if (spawnRow < 0 || spawnRow >= GameSession.getInstance().getArena().getRows()) continue;

            Projectile p = Projectile.spawnNewProjectile(
                plant,
                type,
                damage,
                new Position(spawnCol, spawnRow),
                speedX,
                speedY,
                isPiercingProjectile(type),
                canPassObstacles(plant)
            );
            p.setSpawnDelayTicks(delaySeconds);
        }
    }

    public static int getVolleyCount(String plantName) {
        return switch (plantName) {
            case "Repeater" -> 2;
            case "Mega Gatling Pea" -> 4;
            default -> 1;
        };
    }

    public static void executeTargetedProjectile(Plant plant, Zombie target, float delaySeconds) {
        executeTargetedProjectile(plant, target, delaySeconds, plant.getDamage());
    }

    public static void executeTargetedProjectile(Plant plant, Zombie target, float delaySeconds, int customDamage) {
        ProjectileType type = getProjectileType(plant.getName());
        ProjectileEffect effect = getProjectileEffect(plant.getName());

        if (customDamage == -1 || type == null) return;

        int spawnCol = plant.getPlacedTile().getCol();
        int spawnRow = plant.getPlacedTile().getRow();

        Projectile projectile = Projectile.spawnNewProjectile(
            plant,
            type,
            customDamage,
            new Position(spawnCol, spawnRow),
            0,
            0,
            isPiercingProjectile(type),
            canPassObstacles(plant)
        );

        projectile.setEffect(effect);

        if (ProjectileType.isLobbed(type)) {
            projectile.setArcTrajectory(target, ProjectileTuning.LOB_SPEED_TILES_PER_SEC, ProjectileTuning.LOB_ARC_HEIGHT_TILES);
        } else {
            projectile.setHomingTarget(target, ProjectileTuning.HOMING_SPEED_TILES_PER_SEC);
        }

        projectile.setSpawnDelayTicks(delaySeconds);
    }

    public static ProjectileEffect getProjectileEffect(String name) {
        return switch (name) {
            case "Caulipower" -> new HypnotizeEffect();
            case "Electric Blueberry" -> new LightningEffect();
            case "Citron" -> new AreaOfEffect(new NormalEffect(), 0, 1.5);
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
            case "Citron" -> ProjectileType.PLASMA_BALL;
            case "Kernel-pult" -> ProjectileType.BUTTER;
            case "Cabbage-pult" -> ProjectileType.CABBAGE;
            case "Melon-pult" -> ProjectileType.MELON;
            case "Winter Melon" -> ProjectileType.WINTER_MELON;
            case "Pepper-pult" -> ProjectileType.PEPPER;
            case "Puff-shroom" -> ProjectileType.PUFF_SPORE;
            case "Cactus" -> ProjectileType.ELECTRIC_SPIKE;
            default -> ProjectileType.PEA;
        };
    }



    public static List<float[]> getShotConfigurations(Plant plant) {
        List<float[]> configs = new ArrayList<>();
        String name = plant.getName();

        switch (name) {
            case "Peashooter", "Snow Pea", "Fire Peashooter", "Goo Peashooter", "Sea-shroom", "Puff-shroom",
                 "Repeater", "Mega Gatling Pea", "Pea Pod" ->
                configs.add(new float[]{0, 0, 1, 0});
            case "Threepeater" -> {
                configs.add(new float[]{0, -1, 1, 0}); // top line
                configs.add(new float[]{0, 0, 1, 0});  // middle line
                configs.add(new float[]{0, 1, 1, 0});  // bottom line
            }
            case "Rotobaga" -> {
                configs.add(new float[]{0, 0, 1, -1});  // top-right
                configs.add(new float[]{0, 0, 1, 1});   // bottom-right
                configs.add(new float[]{0, 0, -1, -1}); // top-left
                configs.add(new float[]{0, 0, -1, 1});  // bottom-left
            }
            case "Split Pea" -> {
                configs.add(new float[]{0, 0, 1, 0});  // forward
                configs.add(new float[]{0, 0, -1, 0}); // backward
            }
            case "Starfruit" -> {
                configs.add(new float[]{0, 0, -1, 0});  // backward
                configs.add(new float[]{0, 0, 0, -1});  // up
                configs.add(new float[]{0, 0, 0, 1});   // down
                configs.add(new float[]{0, 0, 1, -1});  // up-right
                configs.add(new float[]{0, 0, 1, 1});   // down-right
            }
        }
        return configs;
    }

    private static boolean isPiercingProjectile(ProjectileType type) {
        return type == ProjectileType.SPIKE;
    }

    private static boolean canPassObstacles(Plant plant) {
        return plant.getCategory() == PlantCategory.LOBBER || plant.getName().equalsIgnoreCase("caulipower");
    }
}
