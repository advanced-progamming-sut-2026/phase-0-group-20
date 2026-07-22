package controllers.GameController;

import models.App;
import models.Result;
import models.enums.Menu;
import models.game.GameSession;
import models.game.adventure.Adventure;
import models.game.adventure.Chapter;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.BonusLevel;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.speciallevels.ConveyorBelt;
import models.game.minigame.BowlingLevel;
import models.users.User;

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

        Level currentLevel = targetChapter.getCurrentLevel(); // for now when we chose a chapter we enter a level that is last unlocked

        if (currentLevel != null && !currentLevel.skipsPlantSelection()) {
            App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        } else if (currentLevel != null) {
            if(currentLevel instanceof ConveyorBelt conveyorBelt){
                GameSession.startNewGame(conveyorBelt.getBelt());
            }else if(currentLevel instanceof BowlingLevel bowlingLevel){
                GameSession.startNewGame(bowlingLevel.getBelt());
            }
            App.setActiveMenu(Menu.GAME_FLOW_MENU);
        }


        return new Result(true, "Enter Chapter " + targetChapter.
                getDisplayName() + " - Level: " + currentLevel.getName());
    }

    public Result enterScoringLevel(boolean isDailyChallenge) {
        Adventure activeAdventure = App.getActiveAdventure();
        Chapter currentChapter = activeAdventure.getCurrentChapter();

        SeasonType season = (currentChapter != null) ? currentChapter.getSeasonType() : SeasonType.ANCIENT_EGYPT;
        int levelNumber = (currentChapter != null) ? currentChapter.getCurrentLevelIndex() + 1 : 1;

        BonusLevel bonusLevel = new BonusLevel("Scoring Challenge", season, 3, 1200, levelNumber, isDailyChallenge);

        GameSession.setPendingBonusLevel(bonusLevel);

        App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        return new Result(true, "Entering Plant Selection for Scoring Mode...");
    }

    public Result enterLeaderboard() {
        App.setActiveMenu(Menu.LEADERBOARD_MENU);
        return new Result(true, "enter leaderboard");
    }

    public Result showCoin() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }
        return new Result(true, "Coins: " + activeUser.getCoin());
    }

    public Result showGem() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }
        return new Result(true, "Diamonds: " + activeUser.getDiamond());
    }

    public Result cheat(int amount, String type) {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }

        if (type.equalsIgnoreCase("coin")) {
            activeUser.earnCoin(amount);
            return new Result(true, amount + " coins added successfully!");
        } else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("gem")) {
            activeUser.earnDiamond(amount);
            return new Result(true, amount + " diamonds added successfully!");
        }

        return new Result(false, "Invalid cheat type! Use 'coin' or 'diamond'.");
    }
}
