package models.entities.plants.effect;

import models.entities.plants.Plant;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
