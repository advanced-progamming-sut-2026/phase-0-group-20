package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.game.GameSession;

/**
 * Sleep Strategy (Day/Night mechanic):
 * If a nocturnal plant (Shroom) is planted during a day level, it falls asleep.
 * Asleep plants cannot perform their usual actions unless woken up (e.g., by Coffee Bean).
 */

public class SleepStrategy implements IPlantStrategy {

    private boolean isAsleep = true;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        // If it's a day level, this DigestionStrategy can block other strategies from executing.
        // In a real implementation, you might want to use a state machine or a flag
        // inside the Plant class (e.g., context.isAsleep()).
    }

    public void wakeUp() {
        this.isAsleep = false;
    }
}
