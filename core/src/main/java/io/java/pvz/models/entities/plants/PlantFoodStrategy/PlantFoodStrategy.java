package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface PlantFoodStrategy {

    void executeStrategy(Plant plant);

    default int getDurationTicks() {
        return 0;
    }

    default void reset() {
    }

}
