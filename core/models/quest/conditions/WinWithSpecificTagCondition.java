package models.quest.conditions;

import models.entities.plants.Plant;
import models.enums.plants.PlantTag;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class WinWithSpecificTagCondition extends QuestCondition {
    boolean isHappened = false;
    private PlantTag plantTag;

    public WinWithSpecificTagCondition(PlantTag plantTag) {
        this.plantTag = plantTag;
    }

    public WinWithSpecificTagCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.LEVEL_COMPLETED) {
            for (Plant plant : payload.getArena().getActivePlants()) {
                if (!plant.getTags().contains(plantTag)) {
                    isHappened = false;
                    return;
                }
            }
            isHappened = true;
        }
    }

    @Override
    public boolean isHappened() {
        return isHappened;
    }
}