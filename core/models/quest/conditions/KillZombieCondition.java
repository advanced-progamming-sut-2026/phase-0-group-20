package models.quest.conditions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("questPlantName")
    private String questPlantName = null;

    @JsonProperty("seasonType")
    private SeasonType seasonType = null;

    @JsonProperty("plantIncluded")
    private boolean plantIncluded = false;

    @JsonProperty("chapterIncluded")
    private boolean chapterIncluded = false;

    private Random random = new Random();

    public KillZombieCondition(int targetKills, String conditionStr) {
        this.targetProgress = targetKills;
        switch (conditionStr) {
            case "Random_Chapter" -> {
                seasonType = randomSeason();
                chapterIncluded = true;
            }
            case "Shooter" -> {
                Plant randomPlant = randomKillerPlant();
                if (randomPlant != null) {
                    questPlantName = randomPlant.getName();
                }
                plantIncluded = true;
            }
            case "Cactus" -> {
                Plant cactus = App.findPlantByName("Cactus");
                if (cactus == null) {
                    throw new IllegalArgumentException("Kill Plant Condition: No such plant exists");
                }
                questPlantName = "Cactus";
                plantIncluded = true;
            }
        }
    }

    public KillZombieCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        if (payload.getType() != GameEvent.ZOMBIE_KILLED) {
            return;
        }

        Zombie zombie = payload.getZombie();
        if (zombie == null) return;

        if (!(plantIncluded || chapterIncluded)) {
            currentProgress++;
        } else if (plantIncluded && payload.getPlant() != null) {
            Plant plant = payload.getPlant();

            if (questPlantName != null && plant.getName().equalsIgnoreCase(questPlantName)) {
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

        if (randomPlants.isEmpty()) return null;

        int rand = random.nextInt(randomPlants.size());
        return randomPlants.get(rand);
    }

    @JsonIgnore
    public SeasonType getSeasonType() {
        return seasonType;
    }
}