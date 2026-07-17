package models.quest.conditions;


import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class QuestCondition implements IQuestCondition {
    protected int currentProgress = 0;
    protected int targetProgress;

    @JsonIgnore
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

    public void resetCurrentProgress() {
        currentProgress = 0;
    }

}
