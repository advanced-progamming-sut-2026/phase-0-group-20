package models;

import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.game.adventure.SeasonType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InGameEntityGenerator {

    public static Plant getPlantForGame(Plant plant, boolean isBoosted) {

        Plant p = PlantFactory.create(plant.getId());
        if (isBoosted) p.setBoosted(true);

        return p;
    }

    public static List<Zombie> getZombiesForLevel(SeasonType season, int level) {
        List<ZombieType> allowedTypes = new ArrayList<>();
        switch (season) {
            case ANCIENT_EGYPT -> allowedTypes = getEgyptZombies(level - 1);
            case FROZEN_CAVES -> allowedTypes = getFrozenCavesZombies(level - 1);
            case DARK_AGES ->  allowedTypes = getDarkAgesZombies(level - 1);
            case BIG_WAVE_BEACH -> allowedTypes = getBeachZombies(level - 1);
            case MINI_GAME -> allowedTypes = getMiniGameZombies(level - 1);
        };

        List<Zombie> levelZombies = new ArrayList<>();
        for (ZombieType type : allowedTypes) {
            levelZombies.add(ZombieFactory.create(type, 0)); // this row is default it will change in wave manager
        }

        return levelZombies;
    }

    private static List<ZombieType> getEgyptZombies(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.RA
            );
            case 1 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.RA,
                    ZombieType.EXPLORER
            );
            // Special Level (ConveyorBelt)
            case 2 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.EXPLORER,
                    ZombieType.TOMB_RAISER,
                    ZombieType.CRYSTAL_SKULL
            );
            // Boss Level
            case 3 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.RA,
                    ZombieType.EXPLORER,
                    ZombieType.TOMB_RAISER,
                    ZombieType.CRYSTAL_SKULL,
                    ZombieType.GARGANTUAR,
                    ZombieType.IMP
            );
            default -> Arrays.asList(ZombieType.NORMAL);
        };
    }

    private static List<ZombieType> getFrozenCavesZombies(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.DODO
            );
            case 1 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.DODO,
                    ZombieType.HUNTER
            );
            // Special Level (DeadLine)
            case 2 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.HUNTER,
                    ZombieType.TROGLOBITE,
                    ZombieType.PROSPECTOR
            );
            // Boss Level
            case 3 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.DODO,
                    ZombieType.HUNTER,
                    ZombieType.TROGLOBITE,
                    ZombieType.PROSPECTOR,
                    ZombieType.BARREL_ROLLER,
                    ZombieType.GARGANTUAR,
                    ZombieType.IMP
            );
            default -> Arrays.asList(ZombieType.NORMAL);
        };
    }

    private static List<ZombieType> getDarkAgesZombies(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.JUGGLER
            );
            case 1 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.DARK_ARMOR,
                    ZombieType.JUGGLER,
                    ZombieType.WIZARD
            );
            // Special Level (LockedPlants)
            case 2 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.DARK_ARMOR,
                    ZombieType.WIZARD,
                    ZombieType.KING,
                    ZombieType.PIANIST
            );
            // Boss Level
            case 3 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.DARK_ARMOR,
                    ZombieType.JUGGLER,
                    ZombieType.WIZARD,
                    ZombieType.KING,
                    ZombieType.PIANIST,
                    ZombieType.ARCADE,
                    ZombieType.GARGANTUAR,
                    ZombieType.IMP_DRAGON
            );
            default -> Arrays.asList(ZombieType.NORMAL);
        };
    }

    private static List<ZombieType> getBeachZombies(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.SNORKEL
            );
            case 1 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.SNORKEL,
                    ZombieType.NEWSPAPER,
                    ZombieType.JANE
            );
            // Special Level (LovePlants)
            case 2 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.BRICK,
                    ZombieType.OCTOPUS,
                    ZombieType.FISHERMAN,
                    ZombieType.ALL_STAR
            );
            // Boss Level
            case 3 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BRICK,
                    ZombieType.SNORKEL,
                    ZombieType.OCTOPUS,
                    ZombieType.FISHERMAN,
                    ZombieType.NEWSPAPER,
                    ZombieType.JANE,
                    ZombieType.ALL_STAR,
                    ZombieType.GARGANTUAR,
                    ZombieType.IMP
            );
            default -> Arrays.asList(ZombieType.NORMAL);
        };
    }

    public static List<Zombie> getZombiesForDailyChallenge() {

        long todaySeed = LocalDate.now().toEpochDay();
        Random dailyRandom = new Random(todaySeed);

        ZombieType[] allowedDailyTypes = ZombieType.values();

        List<ZombieType> todaysEnums = new ArrayList<>();


        int typesToPick = 2 + dailyRandom.nextInt(3);

        while (todaysEnums.size() < typesToPick) {
            ZombieType randomType = allowedDailyTypes[dailyRandom.nextInt(allowedDailyTypes.length)];
            if (!todaysEnums.contains(randomType)) {
                todaysEnums.add(randomType);
            }
        }

        List<Zombie> dailyZombies = new ArrayList<>();
        for (ZombieType type : todaysEnums) {
            dailyZombies.add(getZombieForGame(type, 0));
        }

        return dailyZombies;
    }

    private static List<ZombieType> getMiniGameZombies(int levelIndex) {
        return switch (levelIndex) {
            case 0 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET
            );
            case 1 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.BRICK,
                    ZombieType.NEWSPAPER
            );
            case 2 -> Arrays.asList(
                    ZombieType.NORMAL,
                    ZombieType.CONE,
                    ZombieType.BUCKET,
                    ZombieType.DARK_ARMOR,
                    ZombieType.NEWSPAPER,
                    ZombieType.ALL_STAR
            );
            default -> Arrays.asList(ZombieType.NORMAL);
        };
    }

    public static Zombie getZombieForGame(ZombieType type, int row) {
        return ZombieFactory.create(type, row);
    }
}
