package com.Project.PVZ.models.game.adventure.levels.speciallevels;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.adventure.SeasonType;
import com.Project.PVZ.models.game.adventure.levels.SpecialLevel;

public class TimedWar extends SpecialLevel {
    protected TimedWar(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
    }

    @Override
    public void onLevelStart(GameSession session) {

    }
}
