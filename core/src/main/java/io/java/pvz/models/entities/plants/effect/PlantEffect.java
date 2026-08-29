package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;

public interface PlantEffect {
    void apply(Plant plant);

    void execute(Plant plant, int currentTick);

    void remove(Plant plant);

    boolean isExpired();

    default void notify(String message) {
//        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
//                new GameEventPayload.Builder(GameEvent.NOTIFY)
//                        .message(message)
//                        .build());
    }
}
