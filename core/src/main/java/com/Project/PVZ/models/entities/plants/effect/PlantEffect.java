package com.Project.PVZ.models.entities.plants.effect;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

public interface PlantEffect {
    void apply(Plant plant);

    void execute(Plant plant, int currentTick);

    void remove(Plant plant);

    boolean isExpired();

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }
}
