package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.DeadLineLoseCondition;

public class DeadLine extends SpecialLevel {
    private static final int LOSE_COL = 2;// zero_based

    public DeadLine(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addLoseCondition(new DeadLineLoseCondition(LOSE_COL));
    }

    @Override
    public void onLevelStart(GameSession session) {
        notify("The dead line has been set to the" + (LOSE_COL + 1) + ".");
    }
}
