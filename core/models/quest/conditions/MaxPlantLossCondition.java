package models.quest.conditions;

import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class MaxPlantLossCondition extends QuestCondition {
    boolean lost = false;
    boolean levelCompleted = false;

    public MaxPlantLossCondition(int amount) {
        targetProgress = amount;
    }

    public MaxPlantLossCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.PLANT_LOST) {
            currentProgress++;
            if (currentProgress >= targetProgress) {
                lost = true;
                return;
            }
        } else if (event == GameEvent.LEVEL_COMPLETED) {
            levelCompleted = true;
        }
    }

    @Override
    public boolean isHappened() {
        return levelCompleted && !lost;
    }
}
