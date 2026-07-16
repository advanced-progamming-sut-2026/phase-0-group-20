package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.DeadLineLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

public class DeadLine extends SpecialLevel {
    private final int loseCol;// zero_based

    protected DeadLine(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int loseCol) {
        super(name, season, waveCount, baseWaveDifficulty);
        this.loseCol = loseCol;
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new DeadLineLoseCondition(loseCol));
    }

    @Override
    public void onStart(GameSession session) {
        notify("The dead line has been set to the" + loseCol + 1 + ".");
    }
}
