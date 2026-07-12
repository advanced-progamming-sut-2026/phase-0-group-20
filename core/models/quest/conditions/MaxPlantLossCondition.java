package models.quest.conditions;

import models.game.GameEvent;
import models.game.GameEventPayload;

public class MaxPlantLossCondition extends QuestCondition {
    boolean lost = false;

    public MaxPlantLossCondition(int amount) {
        targetProgress = amount;
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
        }
    }

    @Override
    public boolean isHappened() {
        return !lost;
    }
}
