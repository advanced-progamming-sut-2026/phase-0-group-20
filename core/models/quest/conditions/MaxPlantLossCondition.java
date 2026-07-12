package models.quest.conditions;

import models.entities.plants.Plant;
import models.game.Arena;
import models.game.GameEvent;
import models.game.GameEventPayload;
import models.game.GameSession;

public class MaxPlantLossCondition extends QuestCondition {

    public MaxPlantLossCondition(int amount) {
        targetProgress = amount;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if(event == GameEvent.PLANT_LOST){

        }
    }
}
