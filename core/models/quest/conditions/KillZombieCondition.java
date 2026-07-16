package models.quest.conditions;

import models.App;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantCategory;
import models.game.adventure.SeasonType;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KillZombieCondition extends QuestCondition {
    private Plant questPlant = null;
    private SeasonType seasonType = null;
    private Random random = new Random();
    private boolean plantIncluded = false;
    private boolean chapterIncluded = false;

    public KillZombieCondition(int targetKills, String conditionStr) {
        this.targetProgress = targetKills;
        switch (conditionStr) {
            case "random_chapter" -> {
                seasonType = randomSeason();
                chapterIncluded = true;
            }
            case "Shooter" -> {
                questPlant = randomKillerPlant();
                plantIncluded = true;
            }
            case "Cactus" -> {
                questPlant = App.findPlantByName("Cactus");
                if (questPlant == null) {
                    throw new IllegalArgumentException("Kill Plant Condition:No such plant exists");
                }
                plantIncluded = true;
            }
        }
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        if(payload.getType() != GameEvent.ZOMBIE_KILLED) {
            return;
        }
        Zombie zombie = payload.getZombie();
        if (zombie == null) return;
        if (!(plantIncluded || chapterIncluded)) {
            currentProgress++;
        } else if (plantIncluded) {
            Plant plant = payload.getPlant();
            if (plant.equals(questPlant)) {
                currentProgress++;
            }
        } else if (chapterIncluded) {
            SeasonType desired = payload.getSeasonType();
            if (desired == seasonType) {
                currentProgress++;
            }
        }
    }

    private SeasonType randomSeason() {
        int rand = random.nextInt(4);
        switch (rand) {
            case 0 -> {
                return SeasonType.ANCIENT_EGYPT;
            }
            case 1 -> {
                return SeasonType.BIG_WAVE_BEACH;
            }
            case 2 -> {
                return SeasonType.DARK_AGES;
            }
            case 3 -> {
                return SeasonType.FROZEN_CAVES;
            }
        }
        return SeasonType.ANCIENT_EGYPT;
    }

    private Plant randomKillerPlant() {
        List<Plant> allPlants = App.getAllPlants();
        List<Plant> randomPlants = new ArrayList<>();
        for (Plant plant : allPlants) {
            if (plant.getCategory() != PlantCategory.SUN_PRODUCER &&
                    plant.getCategory() != PlantCategory.MODIFIER &&
                    plant.getCategory() != PlantCategory.WALL_NUT) {
                randomPlants.add(plant);
            }
        }
        int rand = random.nextInt(randomPlants.size());
        return randomPlants.get(rand);

    }
}
