package models.quest.conditions;

import models.entities.plants.Plant;
import models.enums.plants.PlantCategory;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class MaxPlantUsedCondition extends QuestCondition {
    PlantCategory plantCategory;
    boolean lost = false;
    boolean levelCompleted = false;

    public MaxPlantUsedCondition(PlantCategory plantCategory, int amount) {
        this.plantCategory = plantCategory;
        this.targetProgress = amount;
    }

    public MaxPlantUsedCondition() {
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
        } else if (event == GameEvent.LEVEL_COMPLETED) {
            levelCompleted = true;
        }
    }

    @Override
    public boolean isHappened() {
        return levelCompleted && !lost;
    }
}
