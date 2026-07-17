package controllers.GameController;

import models.App;
import models.Result;
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
        return new Result(true, "Coming soon...");
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
            if (quest.getCondition() != null) {
                int current = quest.getCondition().getCurrentProgress();
                int target = quest.getCondition().getTargetProgress();
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

}
