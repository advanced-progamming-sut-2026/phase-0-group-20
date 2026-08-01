package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.game.GameSession;
import models.game.adventure.SeasonType;

/**
 * Sleep Strategy (Day/Night mechanic):
 * If a nocturnal plant (Shroom) is planted during a day level, it falls asleep.
 * Asleep plants cannot perform their usual actions unless woken up (e.g., by Coffee Bean).
 */

public class SleepStrategy implements IPlantStrategy {
    private boolean isInitialized = false;
    @Override
    public void execute(Plant context, int currentTick) {
        if (!isInitialized) {
            SeasonType t = GameSession.getInstance().getCurrentChapter().getSeasonType();
            boolean isDay = false;
            if (t != SeasonType.DARK_AGES) isDay = true;
            if (isDay) {
                context.setAsleep(true);
                GameSession.notify(context.getName() + " fell asleep!");
            }
            isInitialized = true;
        }
    }
}
