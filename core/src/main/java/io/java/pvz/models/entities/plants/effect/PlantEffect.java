package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

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
