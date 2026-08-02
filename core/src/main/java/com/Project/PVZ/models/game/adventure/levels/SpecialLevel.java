package com.Project.PVZ.models.game.adventure.levels;

import com.Project.PVZ.models.game.adventure.SeasonType;
import com.Project.PVZ.models.game.adventure.levels.conditions.NormalLoseCondition;
import com.Project.PVZ.models.game.adventure.levels.conditions.NormalWinCondition;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }
}
