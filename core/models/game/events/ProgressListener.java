package models.game.events;

import models.App;
import models.entities.plants.Plant;
import models.game.GameMode;
import models.game.GameSession;
import models.game.adventure.Adventure;
import models.game.minigame.IMinigame;
import models.game.minigame.MiniGameType;
import models.users.User;

import java.util.ArrayList;
import java.util.List;

public class ProgressListener implements GameEventListener {


    public ProgressListener() {
        registerToAllEvents();
    }

    private void registerToAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values())
            messenger.addListener(event, this);
    }

    public void unregisterFromAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values())
            messenger.removeListener(event, this);

    }


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case LEVEL_COMPLETED:
                handleLevelWon();
                break;
            case GAME_OVER:
                handelLevelLost();
                break;
            default:
                break;
        }
    }

    private void handleLevelWon() {
        User user = App.getActiveUser();
        if (user == null) return;

        user.setGamesPlayed(user.getGamesPlayed() + 1);

        GameMode currentMode = GameSession.getInstance().getCurrentMode();

        if (currentMode instanceof IMinigame miniGame) {
            MiniGameType type = miniGame.getMiniGameType();
            int before = user.getUnlockedLevelInMinigame(type);
            user.unlockNextLevelInMinigame(type);
            if (user.getUnlockedLevelInMinigame(type) > before)
                user.setLevelsCompleted(user.getLevelsCompleted() + 1);

        } else {
            Adventure adventure = App.getActiveAdventure();
            if (adventure != null) {
                int chapterIndex = adventure.getCurrentChapterIndex();
                int levelIndex = adventure.getCurrentChapter().getCurrentLevelIndex();

                if (chapterIndex == user.getHighestUnlockedChapterIndex() &&
                        levelIndex == user.getHighestUnlockedLevelIndex()) {

                    user.setLevelsCompleted(user.getLevelsCompleted() + 1);

                    unlockPlantRewardIfNeeded(user, chapterIndex, levelIndex);

                    adventure.onLevelWon();
                }
            }
        }

    }

    private void handelLevelLost() {
        User activeUser = App.getActiveUser();
        if (activeUser != null)
            activeUser.setGamesPlayed(activeUser.getGamesPlayed() + 1);
    }

    private void unlockPlantRewardIfNeeded(User user, int chapterIndex, int levelIndex) {

        List<Plant> rewardPlant = getRewardsForLevel(chapterIndex, levelIndex);


        for (Plant plant : rewardPlant) {
             user.unlockedPlants(plant);

            GameEventPayload unlockPayload = new GameEventPayload.Builder(GameEvent.PLANT_UNLOCKED)
                    .plant(plant)
                    .message("You unlocked new plant: " + plant.getName())
                    .build();

            GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_UNLOCKED, unlockPayload);
        }
    }

    private List<Plant> getRewardsForLevel(int chapterIndex, int levelIndex) {
        List<Plant> rewards = new ArrayList<>();

        switch (chapterIndex) {
            case 0 -> { // ANCIENT_EGYPT
                if (levelIndex == 0) addRewards(rewards, "Repeater", "Cherry Bomb"); // NORMAL
                else if (levelIndex == 1) addRewards(rewards, "Bonk Choy", "Twin Sunflower"); // NORMAL
                else if (levelIndex == 2) addRewards(rewards, "Split Pea", "Torchwood"); // SPECIAL
                else if (levelIndex == 3) addRewards(rewards, "Threepeater", "Squash"); // BOSS
            }

            case 1 -> { // FROZEN_CAVES
                if (levelIndex == 0) addRewards(rewards, "Hot Potato", "Iceberg Lettuce"); // NORMAL
                else if (levelIndex == 1) addRewards(rewards, "Fire Peashooter", "Snow Pea"); // NORMAL
                else if (levelIndex == 2) addRewards(rewards, "Pepper-pult", "Endurian"); // SPECIAL
                else if (levelIndex == 3) addRewards(rewards, "Jalapeno", "Rotobaga"); // BOSS
            }

            case 2 -> { // DARK_AGES
                if (levelIndex == 0) addRewards(rewards, "Puff-shroom", "Sun-shroom"); // NORMAL
                else if (levelIndex == 1) addRewards(rewards, "Grave Buster", "Fume-shroom"); // NORMAL
                else if (levelIndex == 2) addRewards(rewards, "Magnet-shroom", "Hypno-shroom"); // SPECIAL
                else if (levelIndex == 3) addRewards(rewards, "Doom-shroom", "Sun Bean"); // BOSS
            }

            case 3 -> { // BIG_WAVE_BEACH
                if (levelIndex == 0) addRewards(rewards, "Lily Pad", "Tangle Kelp"); // NORMAL
                else if (levelIndex == 1) addRewards(rewards, "Sea-shroom", "Bowling Bulb"); // NORMAL
                else if (levelIndex == 2) addRewards(rewards, "Sweet Potato", "Chomper"); // SPECIAL
                else if (levelIndex == 3) addRewards(rewards, "Melon-pult", "Tall-nut"); // BOSS
            }
        }
        return rewards;
    }

    private void addRewards(List<Plant> list, String... plantNames) {
        for (String name : plantNames) {
            Plant p = App.findPlantByName(name);
            if (p != null) list.add(p);
        }
    }
}