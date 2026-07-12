package models.quest.conditions;

import models.game.GameEventPayload;

public interface IQuestCondition {

    boolean isHappened();

    void updateProgress(GameEventPayload payload);

    int getCurrentProgress();

    int getTargetProgress();
}
