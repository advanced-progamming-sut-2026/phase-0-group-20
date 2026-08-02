package io.java.pvz.models.game.adventure.levels.conditions;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;

public class NormalLoseCondition implements LoseCondition {
    @Override
    public boolean isLost(GameSession session) {
        return session.isZombieBreached();
    }
}
