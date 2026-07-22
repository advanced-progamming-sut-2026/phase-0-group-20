package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.LovePlantLoseCondition;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;

public class LovePlants extends SpecialLevel {
    private final LovePlantLoseCondition loseCondition;
    private final int LIMIT = 5;

    public LovePlants(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.loseCondition = new LovePlantLoseCondition(LIMIT);
        this.addLoseCondition(loseCondition);

    }

    @Override
    public void onLevelStart(GameSession session) {
        notify("Love Plants Started: do not lose " + LIMIT + " or above number of plants or you lose.");
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_LOST, loseCondition);
    }
}
