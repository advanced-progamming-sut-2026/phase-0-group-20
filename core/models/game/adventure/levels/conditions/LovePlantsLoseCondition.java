package models.game.adventure.levels.conditions;

import models.enums.GameState;
import models.game.LoseCondition;

public class LovePlantsLoseCondition implements LoseCondition {
    @Override
    public boolean isLost(GameState state) {
        return false;
    }
}
