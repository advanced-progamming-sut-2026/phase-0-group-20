package models.quest.conditions;

import models.enums.plants.PlantCategory;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

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
