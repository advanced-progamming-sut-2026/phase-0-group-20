package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.LovePlantLoseCondition;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;

import java.util.Random;

public class LovePlants extends SpecialLevel {
    private final LovePlantLoseCondition loseCondition;
    private final int limit;

    public LovePlants(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        limit = new Random().nextInt(3)+3;
        this.loseCondition = new LovePlantLoseCondition(limit);
        this.addLoseCondition(loseCondition);

    }

    @Override
    public void onLevelStart(GameSession session) {
        notify("Love Plants Started: do not lose " + limit + " or above number of plants or you lose.");
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_LOST, loseCondition);
    }

    @Override
    public void destroyLevelFields() {
        GameEventMessenger.getInstance().removeListener(GameEvent.PLANT_LOST, loseCondition);
    }
}
