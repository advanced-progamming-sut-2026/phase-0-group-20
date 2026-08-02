package com.Project.PVZ.models.quest.conditions;

import com.Project.PVZ.models.enums.plants.PlantCategory;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventPayload;

public class PlantCategoryUseCondition extends QuestCondition {
    PlantCategory plantCategory;

    public PlantCategoryUseCondition(String category, int target) {
        this.plantCategory = PlantCategory.findByName(category);
        targetProgress = target;
    }

    public PlantCategoryUseCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.PLANT_PLACED && payload.getPlant().getCategory() == plantCategory) {
            currentProgress++;
        }
    }


}
