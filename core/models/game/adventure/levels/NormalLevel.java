package models.game.adventure.levels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

public class NormalLevel extends Level {

    protected NormalLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        // Empty for now
    }


}
