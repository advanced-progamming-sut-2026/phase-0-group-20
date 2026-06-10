package models.quest.conditions;

import models.game.GameEvent;

public interface IQuestCondition {

    boolean isHappened();

    void updateProgress(GameEvent event);

    int getCurrentProgress();

    int getTargetProgress();
}
