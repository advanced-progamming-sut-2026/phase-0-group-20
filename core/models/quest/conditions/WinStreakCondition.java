package models.quest.conditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Settings;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class WinStreakCondition extends QuestCondition {

    @JsonCreator
    public WinStreakCondition(@JsonProperty("targetProgress") int targetProgress) {
        this.targetProgress = targetProgress;
        this.currentProgress = 0;
    }

    public WinStreakCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();

        if (event == GameEvent.LEVEL_COMPLETED && Settings.getInstance().getDifficulty() == 5) {
            currentProgress++;

        } else if (event == GameEvent.GAME_OVER) {
            currentProgress = 0;
        }
    }

    @Override
    public boolean isHappened() {
        return currentProgress >= targetProgress;
    }
}