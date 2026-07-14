package models.game.adventure.levels.conditions;

import models.game.GameSession;
import models.game.LoseCondition;

public class LovePlantsLoseCondition implements LoseCondition {

    @Override
    public boolean isLost(GameSession session) {
        return false;
    }
}
