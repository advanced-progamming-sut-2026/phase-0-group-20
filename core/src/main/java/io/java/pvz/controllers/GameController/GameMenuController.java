package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.enums.GameConstants;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.BonusLevel;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.adventure.levels.speciallevels.LockedPlants;
import io.java.pvz.models.game.minigame.BowlingLevel;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class GameMenuController {

    public Result enterChapter(String chapterStr) {
        Adventure activeAdventure = App.getActiveAdventure();
        Chapter targetChapter = activeAdventure.findChapterByName(chapterStr);

        if (targetChapter == null) {
            return new Result(false, "Chapter not found!");
        }

        if (!targetChapter.isUnlocked()) {
            return new Result(false, "This chapter is locked! Complete previous chapters first.");
        }
        App.setActiveMenu(Menu.LEVEL_SELECTION_MENU);
        GameSession.setPendingChapter(targetChapter);
        return new Result(true, "Enter Chapter " + targetChapter.getDisplayName());
    }

    public Result enterLevel(String levelStr) {
        if (GameSession.getPendingChapter() == null)
            return new Result(false, "Choose a Chapter first!");

        int levelNumber;
        try {
            levelNumber = Integer.parseInt(levelStr);
        } catch (Exception e) {
            return new Result(false, "Invalid level number!");
        }

        if (levelNumber < 1 || levelNumber > GameSession.getPendingChapter().getLevels().size()) {
            return new Result(false, "Level " + levelNumber + " does not exist in this chapter.");
        }

        User activeUser = App.getActiveUser();
        int userHighChap = activeUser.getHighestUnlockedChapterIndex();
        int userHighLevel = activeUser.getHighestUnlockedLevelIndex();
        int targetChapterIndex = App.getActiveAdventure().getChapters().indexOf(GameSession.getPendingChapter());

        if (targetChapterIndex == userHighChap && (levelNumber - 1) > userHighLevel)
            return new Result(false, "This level is locked! You need to beat the previous levels first.");

        Level selectedLevel = GameSession.getPendingChapter().getLevels().get(levelNumber - 1);
        GameSession.setPendingLevel(selectedLevel);
        StringBuilder resultText = new StringBuilder();
        resultText.append("Entered Chapter ")
            .append(GameSession.getPendingChapter().getDisplayName())
            .append(" - Level: ").append(selectedLevel.getName()).append("...").append("\n");
        if (selectedLevel instanceof LockedPlants lockLevel) {
            lockLevel.createModEntities();
            resultText.append(lockLevel.createMessage()).append("\n");
        }
        Result result = new Result(true, resultText.toString());
        if (!selectedLevel.skipsPlantSelection()) {
            App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        } else {
            if (selectedLevel instanceof ConveyorBelt conveyorBelt) {
                GameSession.startNewGame(conveyorBelt.getBelt());
            } else if (selectedLevel instanceof BowlingLevel bowlingLevel) {
                GameSession.startNewGame(bowlingLevel.getBelt());
            }
            App.setActiveMenu(Menu.GAME_FLOW_MENU);
        }

        return result;
    }

    public Result enterScoringLevel() {
        Adventure activeAdventure = App.getActiveAdventure();
        Chapter currentChapter = activeAdventure.getCurrentChapter();
        long epochDay = LocalDate.now().toEpochDay();
        Random dailyRandom = new Random(epochDay);
        SeasonType[] allSeasons = SeasonType.values();
        SeasonType season = allSeasons[dailyRandom.nextInt(allSeasons.length)];
        while (season == SeasonType.MINI_GAME) {
            season = allSeasons[dailyRandom.nextInt(allSeasons.length)];
        }

        int maxBound = Math.min(currentChapter.getLevels().size(), currentChapter.getMaxLevelIndexInThisChapter() + 1);
        int levelNumber = dailyRandom.nextInt(Math.max(1, maxBound));

        BonusLevel bonusLevel = new BonusLevel("Scoring Challenge", season, 3, 1200,
            levelNumber, true);

        GameSession.setPendingBonusLevel(bonusLevel);

        App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        return new Result(true, "Entering Plant Selection for Scoring Mode...");
    }

    public Result cheat(int amount, String type) {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }
        if (!App.getSettings().isDebug())
            return new Result(false, "Cheat not allowed!");
        if (type.equalsIgnoreCase("coin")) {
            activeUser.earnCoin(amount);
            return new Result(true, amount + " coins added successfully!");
        } else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("gem")) {
            activeUser.earnDiamond(amount);
            return new Result(true, amount + " diamonds added successfully!");
        }

        return new Result(false, "Invalid cheat type! Use 'coin' or 'diamond'.");
    }

    public String getCurrentMapTextureId() {
        SeasonType type;
        if (GameSession.getPendingChapter() != null) {
            type = GameSession.getPendingChapter().getSeasonType();
        } else if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentChapter() != null) {
            type = GameSession.getInstance().getCurrentChapter().getSeasonType();
        } else if (GameSession.getPendingBonusLevel() != null) {
            type = GameSession.getPendingBonusLevel().getSeason();
        } else {
            type = SeasonType.MINI_GAME;
        }

        return switch (type) {
            case ANCIENT_EGYPT -> Ids.GameMap.ANCIENT_EGYPT_MAIN;
            case FROZEN_CAVES -> Ids.GameMap.ICE_CAVE_MAIN;
            case DARK_AGES -> Ids.GameMap.DARK_AGES_MAIN;
            case BIG_WAVE_BEACH -> Ids.GameMap.BIG_WAVE_MAIN;
            case MINI_GAME -> Ids.GameMap.MINI_GAME_MAIN;
        };
    }
}
