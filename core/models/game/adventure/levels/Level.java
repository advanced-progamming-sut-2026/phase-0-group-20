package models.game.adventure.levels;

import models.enums.GameState;
import models.fields.modifiers.SeasonModifier;
import models.game.Arena;
import models.game.LoseCondition;
import models.game.WinCondition;

import java.util.List;


public abstract class Level {

    protected Arena field;
    protected SeasonModifier seasonModifier;
    protected List<WinCondition> winConditions;
    protected List<LoseCondition> loseConditions;

    public abstract void onStart();
    public abstract void onTick(int tick);

    public void checkResult(GameState state) {
        return;
    }

}
