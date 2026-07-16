package models.game.adventure.levels;

import models.game.adventure.SeasonType;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        super(name, season, waveCount, baseWaveDifficulty, -1);// Agha parham baadan bege ina level daran ya na
    }                                                   // na dadash baeed midonam, ina khodeshon ye no level hastan
}
