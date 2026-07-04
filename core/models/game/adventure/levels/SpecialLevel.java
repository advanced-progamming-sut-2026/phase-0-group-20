package models.game.adventure.levels;

import models.game.adventure.SeasonType;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        super(name, season, waveCount, baseWaveDifficulty);
    }
}
