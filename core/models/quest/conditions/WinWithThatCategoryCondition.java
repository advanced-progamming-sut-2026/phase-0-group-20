package models.quest.conditions;

import models.enums.plants.PlantCategory;
import models.game.GameEvent;
import models.game.GameEventPayload;

public class WinWithThatCategoryCondition extends QuestCondition {
    boolean lost = false;
    boolean withOrWithout;// true for just the family and false for without the family
    PlantCategory plantCategory;

    public WinWithThatCategoryCondition(PlantCategory category, boolean modifier) {
        this.plantCategory = category;
        this.withOrWithout = modifier;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (withOrWithout && !lost) {
            if (event == GameEvent.PLANT_PLACED && payload.getPlant().getCategory() != plantCategory) {
                lost = true;
            }

        } else if (!lost) {
            if (event == GameEvent.PLANT_PLACED && payload.getPlant().getCategory() == plantCategory) {
                lost = true;
            }
        }
    }

    @Override
    public boolean isHappened() {
        return !lost;
    }
}
