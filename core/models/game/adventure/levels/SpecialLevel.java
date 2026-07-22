package models.game.adventure.levels;

import models.game.adventure.SeasonType;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season,int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
    }
}
