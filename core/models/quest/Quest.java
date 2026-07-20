package models.quest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.App;
import models.game.GameSession;
import models.game.events.GameEventPayload;
import models.quest.conditions.IQuestCondition;
import models.quest.conditions.QuestCondition;
import models.quest.reward.Reward;
import models.users.User;

import java.util.UUID;

public class Quest {
    private String id;
    private String title;
    private String description;
    private QuestCategory category;
    private QuestPriority priority;
    private IQuestCondition condition;
    private Reward reward;
    private boolean isCompleted;
    private boolean onMission;

    public Quest(String title, QuestCategory category, QuestPriority priority, boolean onMission, String conditionStr) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
        this.onMission = onMission;
        this.description = conditionStr;

    }

    @JsonCreator
    public Quest(@JsonProperty("id") String id,
                 @JsonProperty("title") String title,
                 @JsonProperty("description") String description,
                 @JsonProperty("category") QuestCategory category,
                 @JsonProperty("priority") QuestPriority priority,
                 @JsonProperty("condition") QuestCondition condition,
                 @JsonProperty("reward") Reward reward,
                 @JsonProperty("completed") boolean isCompleted,
                 @JsonProperty("onMission") boolean onMission) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.condition = condition;
        this.reward = reward;
        this.isCompleted = isCompleted;
        this.onMission = onMission;
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

            User activeUser = App.getActiveUser();
            if (activeUser != null && reward != null) {
                reward.claimReward(activeUser);
            }
            String message = "Quest Completed: " + title + "! You gained " + reward.toString();
            GameSession.notify(message);
        }
    }

    public String getDescription() {
        return description;
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

    public boolean isOnMission() {
        return onMission;
    }
}