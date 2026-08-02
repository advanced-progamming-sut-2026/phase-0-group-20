package com.Project.PVZ.models.game.adventure.levels;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.adventure.SeasonType;
import com.Project.PVZ.models.game.adventure.levels.conditions.NormalLoseCondition;
import com.Project.PVZ.models.game.adventure.levels.conditions.NormalWinCondition;

public class NormalLevel extends Level {

    protected NormalLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        super(name, season, waveCount, baseWaveBudget, levelNumber);
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {
        // Empty for now
    }


}
