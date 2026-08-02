package io.java.pvz.models.quest.conditions;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;

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
