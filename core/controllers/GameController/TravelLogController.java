package controllers.GameController;

import controllers.NavigationController;
import models.App;
import models.Result;
import models.entities.plants.Plant;
import models.enums.Menu;
import models.game.GameSession;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.speciallevels.ConveyorBelt;
import models.game.minigame.BowlingLevel;
import models.game.minigame.MiniGameFactory;
import models.game.minigame.MiniGameType;
import models.game.minigame.VaseBreakerLevel;
import models.quest.Quest;
import models.quest.QuestCategory;
import models.quest.QuestManager;
import models.users.User;

import java.util.ArrayList;
import java.util.List;

public class TravelLogController {
    enum ValidPageNames {
        DAILY,
        MAIN,
        EPIC,
        MINIGAME;
    }

    private User activeUser;
    private ValidPageNames currentPage = ValidPageNames.DAILY;

    public TravelLogController() {

    }

    public Result showTheCatalog() {
        activeUser = App.getActiveUser();
        StringBuilder catalog = new StringBuilder();
        QuestCategory[] categories = QuestCategory.values();

        for (int i = 0; i < categories.length; i++) {
            int pageNum = i + 1;
            QuestCategory currentCategory = categories[i];

            if (currentCategory == QuestCategory.MINIGAME) {
                catalog.append("Page ").append(pageNum).append(" : go to minigames");
            } else {
                catalog.append("Page ").append(pageNum).append(" : ")
                        .append(currentCategory.name().toLowerCase());
            }

            if (i < categories.length - 1) {
                catalog.append("\n");
            }
        }

        return new Result(true, catalog.toString());
    }

    public Result changePage(String pageName) {
        activeUser = App.getActiveUser();
        pageName = pageName.toUpperCase();
        if (ValidPageNames.valueOf(pageName) == null) {
            return new Result(false, "Invalid page name\nValid page names:\n" +
                    "1.daily\n2.main\n3.epic\n4.minigame");
        }
        ValidPageNames nextPage = ValidPageNames.valueOf(pageName);
        if (nextPage == currentPage) {
            return new Result(false, "You are already in this page.");
        }
        currentPage = nextPage;
        return new Result(true, "Changed the page to the " + currentPage.name().toLowerCase());

    }

    public Result showCurrentPage() {
        if (currentPage != ValidPageNames.MINIGAME)
            return showCategoryQuests();
        return new Result(true, buildMinigamesView());
    } // temporary

    private Result showCategoryQuests() {
        activeUser = App.getActiveUser();
        QuestManager questManager = activeUser.getQuestManager();
        List<Quest> quests = new ArrayList<>();
        QuestCategory category = findQuestCategory();
        for (Quest quest : questManager.getActiveQuests()) {
            if (quest.getCategory() == category) {
                quests.add(quest);
            }
        }

        if (quests.isEmpty()) {
            return new Result(true, "No quests available in the " + category.name().toLowerCase() + " category.");
        }

        StringBuilder pageDisplay = new StringBuilder();
        pageDisplay.append("--- ").append(category.name()).append(" Quests ---\n");

        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);

            String rewardText = (quest.getReward() != null) ? quest.getReward().toString() : "No Reward";

            String progressText = "0/0";
            if (quest.isCompleted()) {
                progressText = "Completed!";
            } else if (quest.getCondition() != null) {
                int current = quest.getCondition().getCurrentProgress();
                int target = quest.getCondition().getTargetProgress();
                if (target == 0) target = 1;
                progressText = current + "/" + target;
            }

            pageDisplay.append(i + 1).append(". ").append(quest.getTitle()).append("\n")
                    .append("   Description: ").append(quest.getDescription()).append("\n")
                    .append("   Progress:    [").append(progressText).append("]\n")
                    .append("   Reward:      ").append(rewardText).append("\n\n");
        }

        if (pageDisplay.length() > 0) {
            pageDisplay.setLength(pageDisplay.length() - 2);
        }

        return new Result(true, pageDisplay.toString());
    }

    private QuestCategory findQuestCategory() {
        return switch (currentPage) {
            case MAIN -> QuestCategory.MAIN;
            case EPIC -> QuestCategory.EPIC;
            case MINIGAME -> QuestCategory.MINIGAME;
            case DAILY -> QuestCategory.DAILY;
        };
    }


    public Result startMiniGame(String miniGameName, String levelString) {
        int levelNumber;
        try {
            levelNumber = Integer.parseInt(levelString);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid level number");
        }
        if (currentPage != ValidPageNames.MINIGAME)
            return new Result(false, "You must be in the MINIGAME page to start a minigame.");

        if (levelNumber < 1 || levelNumber > 3)
            return new Result(false, "Invalid level number! Minigames only have levels 1, 2, and 3.");

        try {

            MiniGameType type = MiniGameType.findByName(miniGameName);

            int maxUnlocked = activeUser.getUnlockedLevelInMinigame(type);
            if (levelNumber - 1 > maxUnlocked)
                return new Result(false, "Level " + levelNumber + " is LOCKED! You must beat level " + (levelNumber - 1) + " first.");

            Level minigameLevel = MiniGameFactory.createLevel(type, levelNumber);

            GameSession.setMinigameLevel(minigameLevel);

            if (!minigameLevel.skipsPlantSelection()) {
                App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
            } else {
                List<Plant> inGamePlants = null;
                if (minigameLevel instanceof BowlingLevel bowling)
                    inGamePlants = bowling.getBelt();

                App.setActiveMenu(Menu.GAME_FLOW_MENU);
                GameSession.startMiniGame(minigameLevel, inGamePlants);

            }

            NavigationController.enterMenu("game flow menu");

            return new Result(true, "Started " + type.getName() + " Level " + (levelNumber) + "! Good luck!");

        } catch (IllegalArgumentException e) {
            return new Result(false, "Invalid minigame name! Available: VASE_BREAKER, BOWLING, I_ZOMBIE");
        } catch (NullPointerException e) {
            return new Result(false, "Invalid minigame name!");
        } catch (Exception e) {
            return new Result(false, "Something bad happened. Please try again.");
        }
    }

    private String buildMinigamesView() {
        StringBuilder sb = new StringBuilder();

        sb.append("--- Available Minigames ---\n");
        MiniGameType[] minigames = MiniGameType.values();

        if (minigames.length == 0) {
            sb.append("No minigames unlocked yet.\n");
            return sb.toString().trim();
        }

        for (int i = 0; i < minigames.length; i++) {
            String rawName = minigames[i].name().replace("_", " ").toLowerCase();
            String prettyName = rawName.substring(0, 1).toUpperCase() + rawName.substring(1);

            sb.append(i + 1).append(". ").append(prettyName).append("\n");
        }

        return sb.toString().trim();
    }

}
