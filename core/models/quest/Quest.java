package models.quest;

import models.App;
import models.game.GameEventPayload;
import models.quest.conditions.IQuestCondition;
import models.quest.reward.Reward;
import models.users.User;

import java.util.UUID;

public class Quest {
    private String id;
    private String title;
    private QuestCategory category;
    private QuestPriority priority;
    private IQuestCondition condition;
    private Reward reward;
    private boolean isCompleted;

    public Quest(String title, QuestCategory category, QuestPriority priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
    }

    public void onEvent(GameEventPayload payload) {
        if (isCompleted || condition == null) {
            return;
        }

        condition.updateProgress(payload);
        if (condition.isHappened()) {
            complete();
        }
    }

    public void complete() {
        if (!isCompleted) {
            this.isCompleted = true;
            System.out.println("Quest Completed: " + title + "!"); // for testing

            User activeUser = App.getActiveUser();
            if (activeUser != null && reward != null) {
                reward.claimReward(activeUser);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public QuestPriority getPriority() {
        return priority;
    }

    public IQuestCondition getCondition() {
        return condition;
    }

    public void setCondition(IQuestCondition condition) {
        this.condition = condition;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}