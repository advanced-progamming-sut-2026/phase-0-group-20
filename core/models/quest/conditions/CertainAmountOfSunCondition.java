package models.quest.conditions;

import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class CertainAmountOfSunCondition extends QuestCondition {

    public CertainAmountOfSunCondition(int amount) {
        targetProgress = amount;
    }
    public CertainAmountOfSunCondition() {}
    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.LEVEL_COMPLETED && GameSession.getInstance().getCurrentSun() == targetProgress) {
            currentProgress = targetProgress;
        }
    }
}
