package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.LovePlantLoseCondition;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;

public class LovePlants extends SpecialLevel {
    private final LovePlantLoseCondition loseCondition;
    private final int limit;

    protected LovePlants(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int limit) {
        super(name, season, waveCount, baseWaveDifficulty);
        this.limit = limit;
        this.loseCondition = new LovePlantLoseCondition(limit);
        this.addLoseCondition(loseCondition);

    }

    @Override
    public void onStart(GameSession session) {
        notify("Love Plants Started: do not lose " + limit + " or above number of plants or you lose.");
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_LOST, loseCondition);
    }
}
