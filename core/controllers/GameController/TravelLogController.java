package controllers.GameController;

import controllers.NavigationController;
import models.App;
import models.Result;
import models.entities.plants.Plant;
import models.enums.Menu;
import models.game.GameSession;
import models.game.adventure.levels.Level;
import models.game.minigame.BowlingLevel;
import models.game.minigame.MiniGameFactory;
import models.game.minigame.MiniGameType;
import models.quest.Quest;
import models.quest.QuestCategory;
import models.users.User;

import java.util.List;

public class TravelLogController {
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
        ValidPageNames nextPage = findByName(pageName);
        if (nextPage == null) {
            return new Result(false, "Invalid page name\nValid page names:\n" +
                    "1.daily\n2.main\n3.epic\n4.minigame");
        }
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
    }


    private Result showCategoryQuests() {
        activeUser = App.getActiveUser();
        QuestCategory category = findQuestCategory();

        List<Quest> quests = activeUser.getQuestManager().getActiveQuests().stream()
                .filter(q -> q.getCategory() == category)
                .sorted(java.util.Comparator.comparing(Quest::getPriority))
                .toList();

        if (quests.isEmpty()) {
            return new Result(true, "No quests available in the " + category.name().toLowerCase() + " category.");
        }

        return new Result(true, buildQuestDisplayString(quests, category));
    }

    private String buildQuestDisplayString(List<Quest> quests, QuestCategory category) {
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
                int target = Math.max(1, quest.getCondition().getTargetProgress());
                progressText = current + "/" + target;
            }

            pageDisplay.append(i + 1).append(". ").append(quest.getTitle()).append("\n")
                    .append("   Priority:    ").append(quest.getPriority()).append("\n")
                    .append("   Description: ").append(quest.getDescription()).append("\n")
                    .append("   Progress:    [").append(progressText).append("]\n")
                    .append("   Reward:      ").append(rewardText).append("\n\n");
        }

        return pageDisplay.substring(0, pageDisplay.length() - 2);
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
                return new Result(false, "Level " + levelNumber + " is LOCKED!" +
                        " You must beat level " + (levelNumber - 1) + " first.");

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
            return new Result(false, "Invalid minigame name! Available: vasebreaker, bowling, izombie, beghouled, zombotany");
        } catch (NullPointerException e) {
            return new Result(false, "Invalid minigame name!");
        } catch (Exception e) {
            return new Result(false, "Something bad happened. Please try again.");
        }
    }

    public Result printMiniGames() {
        if (currentPage != ValidPageNames.MINIGAME)
            return new Result(false, "You must be in the MINIGAME page to view the minigames map.");

        activeUser = App.getActiveUser();
        if (activeUser == null)
            return new Result(false, "No active user found!");

        StringBuilder sb = new StringBuilder();
        sb.append("\n--- MiniGames Map ---\n\n");

        MiniGameType[] minigames = MiniGameType.values();
        for (int i = 0; i < minigames.length; i++) {
            MiniGameType type = minigames[i];

            String rawName = type.name().replace("_", " ").toLowerCase();
            String prettyName = rawName.substring(0, 1).toUpperCase() + rawName.substring(1);

            String prefix = String.format("%d. %-15s -> ", i + 1, prettyName);

            StringBuilder boxRow = new StringBuilder(prefix);
            StringBuilder labelRow = new StringBuilder(" ".repeat(prefix.length()));

            int completedLevels = activeUser.getUnlockedLevelInMinigame(type);
            int totalLevels = 3;

            for (int j = 1; j <= totalLevels; j++) {

                boolean isUnlocked = (j <= completedLevels + 1);

                String box = isUnlocked ? "[Unlocked]" : "[ Locked ]";
                String lbl = String.format("  Lvl %-4d", j);

                boxRow.append(box);
                labelRow.append(lbl);

                if (j < totalLevels) {
                    boxRow.append("--");
                    labelRow.append("  ");
                }
            }

            sb.append(boxRow.toString()).append("\n");
            sb.append(labelRow.toString()).append("\n\n");
        }

        return new Result(true, sb.toString().trim());
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

    enum ValidPageNames {
        DAILY,
        MAIN,
        EPIC,
        MINIGAME;
    }

    private ValidPageNames findByName(String name) {
        for (ValidPageNames page : ValidPageNames.values()) {
            if (page.name().equalsIgnoreCase(name))
                return page;

        }
        return null;
    }

}
