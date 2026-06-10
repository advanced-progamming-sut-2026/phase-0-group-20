package models.quest.conditions;


import models.game.GameEvent;

public abstract class QuestCondition implements IQuestCondition {
    protected int currentProgress=0;
    protected int targetProgress;

    @Override
    public boolean isHappened() {
        return currentProgress >= targetProgress;
    }
    @Override
    public int getCurrentProgress() {
        return currentProgress;
    }

    @Override
    public int getTargetProgress() {
        return targetProgress;
    }

}
