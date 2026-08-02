package com.Project.PVZ.models.entities.plants.PlantFoodStrategy;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

public interface PlantFoodStrategy {

    void executeStrategy(Plant plant);

    default int getDurationTicks() {
        return 0;
    }

    default void reset() {
    }

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }
}
