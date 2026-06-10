package models.quest;

import models.game.GameEvent;
import models.game.GameEventPayload;
import models.quest.conditions.IQuestCondition;
import models.quest.reward.Reward;

public class Quest {
    private  String id;
    private  String title;
    private  QuestCategory category;
    private  Priority priority;
    private IQuestCondition condition;
    private  Reward reward;
    private boolean isCompleted;
    public void handleEventHappened(GameEvent event){

    }

    public void complete(){

    }

    public void onEvent(GameEventPayload payload){

    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public QuestCategory getCategory() { return category; }
    public Priority getPriority() { return priority; }
    public IQuestCondition getCondition() { return condition; }
    public Reward getReward() { return reward; }
    public boolean isCompleted() { return isCompleted; }
}
