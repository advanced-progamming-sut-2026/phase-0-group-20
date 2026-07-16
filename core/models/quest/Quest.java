package models.quest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.App;
import models.game.events.GameEventPayload;
import models.quest.conditions.IQuestCondition;
import models.quest.conditions.QuestCondition;
import models.quest.reward.Reward;
import models.users.User;

import java.util.UUID;

public class Quest {
    private String id;
    private String title;
    private QuestCategory category;
    private QuestPriority priority;
    private QuestCondition condition;
    private Reward reward;
    private boolean isCompleted;
    private boolean onMission;
    public Quest(String title, QuestCategory category, QuestPriority priority, boolean onMission) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
        this.onMission = onMission;
    }

    @JsonCreator
    public Quest(@JsonProperty("id") String id,
                 @JsonProperty("title") String title,
                 @JsonProperty("category") QuestCategory category,
                 @JsonProperty("priority") QuestPriority priority,
                 @JsonProperty("condition") QuestCondition condition,
                 @JsonProperty("reward") Reward reward,
                 @JsonProperty("completed") boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.condition = condition;
        this.reward = reward;
        this.isCompleted = isCompleted;
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

    public QuestCondition getCondition() {
        return condition;
    }

    public void setCondition(QuestCondition condition) {
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

    public boolean isOnMission() {
        return onMission;
    }
}