package io.java.pvz.models.quest.conditions;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;

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
