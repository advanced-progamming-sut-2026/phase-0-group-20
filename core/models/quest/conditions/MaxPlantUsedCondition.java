package models.quest.conditions;

import models.entities.plants.Plant;
import models.enums.plants.PlantCategory;
import models.game.GameEvent;
import models.game.GameEventPayload;

public class MaxPlantUsedCondition extends QuestCondition {
    PlantCategory plantCategory;
    boolean lost = false;

    public MaxPlantUsedCondition(PlantCategory plantCategory, int amount) {
        this.plantCategory = plantCategory;
        this.targetProgress = amount;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.PLANT_PLACED) {
            Plant placedPlant = payload.getPlant();
            if (placedPlant.getCategory() == plantCategory) {
                currentProgress++;
                if (currentProgress > targetProgress) {
                    lost = true;
                    return;
                }
            }
        }
    }

    @Override
    public boolean isHappened() {
        return !lost;
    }
}
