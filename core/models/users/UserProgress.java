package models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.plants.PlantSaveData;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieSaveData;
import models.game.minigame.MiniGameType;
import models.news.Message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UserProgress {
    private int gamesPlayed;
    private int levelsCompleted;
    private int highestBonusScore;
    private int desiredDifficulty;
    private int highestUnlockedChapterIndex;
    private int highestUnlockedLevelIndex;

    private final ArrayList<Zombie> unlockedZombies = new ArrayList<>();
    private final ArrayList<Plant> unlockedPlants = new ArrayList<>();
    private Map<MiniGameType, Integer> unlockedMinigames = new EnumMap<>(MiniGameType.class);

    public UserProgress() {
        this.gamesPlayed = 0;
        this.levelsCompleted = 0;
        this.highestBonusScore = 0;
        this.desiredDifficulty = 3;
        this.highestUnlockedChapterIndex = 0;
        this.highestUnlockedLevelIndex = 0;

        for (MiniGameType type : MiniGameType.values()) {
            this.unlockedMinigames.put(type, 0);
        }

        unlockedPlants.clear();
        unlockedPlants.add(PlantFactory.create(1)); // Sunflower
        unlockedPlants.add(PlantFactory.create(6)); // Peashooter
        unlockedPlants.add(PlantFactory.create(44)); // Wall-nut
        unlockedPlants.add(PlantFactory.create(30)); // Potato Mine
        unlockedPlants.add(PlantFactory.create(25)); // Cabbage-pult
    }

    @JsonCreator
    public UserProgress(@JsonProperty("gamesPlayed") int gamesPlayed,
                        @JsonProperty("levelsCompleted") int levelsCompleted,
                        @JsonProperty("highestBonusScore") int highestBonusScore,
                        @JsonProperty("desiredDifficulty") int desiredDifficulty,
                        @JsonProperty("highestUnlockedChapterIndex") int highestUnlockedChapterIndex,
                        @JsonProperty("highestUnlockedLevelIndex") int highestUnlockedLevelIndex,
                        @JsonProperty("unlockedMinigames") Map<MiniGameType, Integer> unlockedMinigames) {
        this.gamesPlayed = gamesPlayed;
        this.levelsCompleted = levelsCompleted;
        this.highestBonusScore = highestBonusScore;
        this.desiredDifficulty = desiredDifficulty;
        this.highestUnlockedChapterIndex = highestUnlockedChapterIndex;
        this.highestUnlockedLevelIndex = highestUnlockedLevelIndex;
        this.unlockedMinigames = (unlockedMinigames != null) ? unlockedMinigames : new EnumMap<>(MiniGameType.class);

        if (this.unlockedMinigames.isEmpty()) {
            for (MiniGameType type : MiniGameType.values()) {
                this.unlockedMinigames.put(type, 0);
            }
        }
    }

    public void unlockNextLevelInMinigame(MiniGameType type, User user) {
        int currentUnlocked = getUnlockedLevelInMinigame(type);
        if (currentUnlocked < 3) {
            unlockedMinigames.put(type, currentUnlocked + 1);
            if (user != null) {
                user.addMessage(Message.minigameUnlockedMessage(type.getName()));
            }
        }
    }

    public void addZombiesToUnlock(List<Zombie> inGameZombies, User user) {
        for (Zombie zombie : inGameZombies) {
            if (!this.unlockedZombies.contains(zombie)) {
                this.unlockedZombies.add(zombie);
                if (user != null) user.addMessage(Message.zombieUnlockedMessage(zombie));
            }
        }
    }

    public void unlockAdventureLevel(int targetChapterIndex, int targetLevelIndex, String chapterName, User user) {
        if (targetChapterIndex > highestUnlockedChapterIndex ||
                (targetChapterIndex == highestUnlockedChapterIndex && targetLevelIndex > highestUnlockedLevelIndex)) {
            highestUnlockedChapterIndex = targetChapterIndex;
            highestUnlockedLevelIndex = targetLevelIndex;

            if (user != null) {
                if (targetLevelIndex == 0) {
                    user.addMessage(Message.chapterUnlockedMessage(chapterName));
                } else {
                    user.addMessage(Message.levelUnlockedMessage(chapterName, targetLevelIndex + 1));
                }
            }
        }
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public void setLevelsCompleted(int levelsCompleted) {
        this.levelsCompleted = levelsCompleted;
    }

    public int getHighestBonusScore() {
        return highestBonusScore;
    }

    public void setHighestBonusScore(int highestBonusScore) {
        this.highestBonusScore = highestBonusScore;
    }

    public int getDesiredDifficulty() {
        return desiredDifficulty;
    }

    public void setDesiredDifficulty(int desiredDifficulty) {
        this.desiredDifficulty = desiredDifficulty;
    }

    public int getHighestUnlockedChapterIndex() {
        return highestUnlockedChapterIndex;
    }

    public void setHighestUnlockedChapterIndex(int index) {
        this.highestUnlockedChapterIndex = index;
    }

    public int getHighestUnlockedLevelIndex() {
        return highestUnlockedLevelIndex;
    }

    public void setHighestUnlockedLevelIndex(int index) {
        this.highestUnlockedLevelIndex = index;
    }

    public Map<MiniGameType, Integer> getUnlockedMinigames() {
        return unlockedMinigames;
    }

    public void setUnlockedMinigames(Map<MiniGameType, Integer> unlockedMinigames) {
        if (unlockedMinigames != null) this.unlockedMinigames = unlockedMinigames;
    }

    public int getUnlockedLevelInMinigame(MiniGameType type) {
        return unlockedMinigames.getOrDefault(type, 1);
    }

    @JsonIgnore
    public ArrayList<Zombie> getUnlockedZombies() {
        return unlockedZombies;
    }

    @JsonIgnore
    public void setUnlockedZombies(ArrayList<Zombie> zombies) {
        if (zombies != null) {
            this.unlockedZombies.clear();
            this.unlockedZombies.addAll(zombies);
        }
    }

    @JsonProperty("unlockedZombiesData")
    public ArrayList<ZombieSaveData> getUnlockedZombiesData() {
        ArrayList<ZombieSaveData> data = new ArrayList<>();
        for (Zombie zombie : unlockedZombies) data.add(new ZombieSaveData(zombie.getType()));
        return data;
    }

    @JsonProperty("unlockedZombiesData")
    public void setUnlockedZombiesData(ArrayList<ZombieSaveData> dataList) {
        this.unlockedZombies.clear();
        if (dataList != null) {
            for (ZombieSaveData data : dataList) {
                Zombie rebuiltZombie = ZombieFactory.create(data.getType(), -1);
                if (rebuiltZombie != null) this.unlockedZombies.add(rebuiltZombie);
            }
        }
    }

    @JsonIgnore
    public ArrayList<Plant> getUnlockedPlants() {
        return unlockedPlants;
    }

    @JsonIgnore
    public void setUnlockedPlants(ArrayList<Plant> plants) {
        if (plants != null) {
            this.unlockedPlants.clear();
            this.unlockedPlants.addAll(plants);
        }
    }

    @JsonProperty("unlockedPlantsData")
    public ArrayList<PlantSaveData> getUnlockedPlantsData() {
        ArrayList<PlantSaveData> data = new ArrayList<>();
        for (Plant plant : unlockedPlants)
            data.add(new PlantSaveData(plant.getId(), plant.getLevel(), plant.isBoosted()));
        return data;
    }

    @JsonProperty("unlockedPlantsData")
    public void setUnlockedPlantsData(ArrayList<PlantSaveData> dataList) {
        this.unlockedPlants.clear();
        if (dataList != null) {
            for (PlantSaveData data : dataList) {
                Plant rebuiltPlant = PlantFactory.create(data.getId());
                if (rebuiltPlant != null) {
                    rebuiltPlant.setBoosted(data.isBoosted());
                    while (rebuiltPlant.getLevel() < data.getLevel()) rebuiltPlant.upgrade();
                    this.unlockedPlants.add(rebuiltPlant);
                }
            }
        }
    }
}