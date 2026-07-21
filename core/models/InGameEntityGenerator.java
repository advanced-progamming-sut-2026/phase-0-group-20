package models;

import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.game.adventure.SeasonType;

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
            case ANCIENT_EGYPT -> allowedTypes = switch (level) {
                case 1 -> Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.RA);
                case 2 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.RA, ZombieType.EXPLORER);
                case 3 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.BUCKET, ZombieType.BRICK, ZombieType.EXPLORER, ZombieType.TOMB_RAISER);
                default ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.RA, ZombieType.EXPLORER, ZombieType.TOMB_RAISER, ZombieType.GARGANTUAR);
            };

            case FROZEN_CAVES -> allowedTypes = switch (level) {
                case 1 -> Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.DODO);
                case 2 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.DODO, ZombieType.HUNTER);
                case 3 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.BUCKET, ZombieType.NEWSPAPER, ZombieType.HUNTER, ZombieType.TROGLOBITE);
                default ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.DODO, ZombieType.HUNTER, ZombieType.TROGLOBITE, ZombieType.GARGANTUAR);
            };

            case BIG_WAVE_BEACH -> allowedTypes = switch (level) {
                case 1 -> Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.SNORKEL);
                case 2 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.SNORKEL, ZombieType.FISHERMAN);
                case 3 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.BUCKET, ZombieType.BRICK, ZombieType.FISHERMAN, ZombieType.OCTOPUS);
                default ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.SNORKEL, ZombieType.FISHERMAN, ZombieType.OCTOPUS, ZombieType.GARGANTUAR);
            };

            case DARK_AGES -> allowedTypes = switch (level) {
                case 1 -> Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.IMP_DRAGON);
                case 2 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.DARK_ARMOR, ZombieType.IMP_DRAGON, ZombieType.JUGGLER);
                case 3 ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.DARK_ARMOR, ZombieType.BRICK, ZombieType.JUGGLER, ZombieType.WIZARD);
                default ->
                        Arrays.asList(ZombieType.NORMAL, ZombieType.CONE, ZombieType.DARK_ARMOR, ZombieType.JUGGLER, ZombieType.WIZARD, ZombieType.KING, ZombieType.GARGANTUAR);
            };
        }

        List<Zombie> levelZombies = new ArrayList<>();
        for (ZombieType type : allowedTypes) {
            levelZombies.add(ZombieFactory.create(type, 0)); // this row is default it will change in wave manager
        }

        return levelZombies;
    }

    public static List<Zombie> getZombiesForDailyChallenge() {

        long todaySeed = java.time.LocalDate.now().toEpochDay();
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

    public static Zombie getZombieForGame(ZombieType type, int row) {
        return ZombieFactory.create(type, row);
    }
}
