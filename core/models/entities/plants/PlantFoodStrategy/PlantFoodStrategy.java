package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
