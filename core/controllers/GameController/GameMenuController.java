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
import models.game.adventure.levels.speciallevels.LockedPlants;
import models.game.minigame.BowlingLevel;
import models.users.User;

import java.util.List;

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
        if(selectedLevel instanceof LockedPlants lockLevel){
            lockLevel.createModEntities();
            resultText.append(lockLevel.createMessage()).append("\n");
        }
        Result result = new Result(true,resultText.toString());
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

        SeasonType season = (currentChapter != null) ? currentChapter.getSeasonType() : SeasonType.ANCIENT_EGYPT;
        int levelNumber = (currentChapter != null) ? currentChapter.getCurrentLevelIndex() + 1 : 1;

        BonusLevel bonusLevel = new BonusLevel("Scoring Challenge", season, 3, 1200,
                levelNumber, true);

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


    public Result printAdventure() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }

        Adventure adventure = App.getActiveAdventure();
        if (adventure == null) {
            return new Result(false, "No active adventure found!");
        }

        int userHighChap = activeUser.getHighestUnlockedChapterIndex();
        int userHighLevel = activeUser.getHighestUnlockedLevelIndex();

        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Adventure Map ---\n\n");

        List<Chapter> chapters = adventure.getChapters();
        int displayIndex = 1;

        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);

            if (chapter.getSeasonType() != null && chapter.getSeasonType().name().equals("MINI_GAME")) {
                continue;
            }

            sb.append(formatChapter(chapter, displayIndex++, i, userHighChap, userHighLevel));
        }

        return new Result(true, sb.toString().trim());
    }

    private String formatChapter(Chapter chapter, int displayIndex, int chapterIndex,
                                 int userHighChap, int userHighLevel) {
        String chapterName = chapter.getDisplayName();
        if (chapterName == null) chapterName = "Unknown";

        String prefix = String.format("Chapter %d: %-15s -> ", displayIndex, chapterName);

        StringBuilder boxRow = new StringBuilder(prefix);
        StringBuilder labelRow = new StringBuilder(" ".repeat(prefix.length()));

        int totalLevels = 4;
        if (chapter.getLevels() != null && !chapter.getLevels().isEmpty()) {
            totalLevels = chapter.getLevels().size();
        }

        for (int j = 0; j < totalLevels; j++) {
            boolean isUnlocked = false;
            if (chapterIndex < userHighChap) {
                isUnlocked = true;
            } else if (chapterIndex == userHighChap && j <= userHighLevel) {
                isUnlocked = true;
            }

            String box = isUnlocked ? "[Unlocked]" : "[ Locked ]";
            String lbl = String.format("  Lvl %-4d", j + 1);

            boxRow.append(box);
            labelRow.append(lbl);

            if (j < totalLevels - 1) {
                boxRow.append("--");
                labelRow.append("  ");
            }
        }

        return boxRow.toString() + "\n" + labelRow.toString() + "\n\n";
    }

}
