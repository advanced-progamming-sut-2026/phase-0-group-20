package models.game.adventure.levels;

import models.game.adventure.SeasonType;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }
}
