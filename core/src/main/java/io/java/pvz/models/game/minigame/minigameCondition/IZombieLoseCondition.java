package io.java.pvz.models.game.minigame.minigameCondition;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;

public class IZombieLoseCondition implements LoseCondition {
    @Override
    public boolean isLost(GameSession session) {
        if (!session.getArena().getActiveZombies().isEmpty())
            return false;

        int cheapestZombieCost = Integer.MAX_VALUE;
        for (Zombie zombie : session.getChosenZombies())
            if (zombie.getWaveCost() < cheapestZombieCost)
                cheapestZombieCost = zombie.getWaveCost();

        return session.getCurrentSun() < cheapestZombieCost;
    }

}
