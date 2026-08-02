package com.Project.PVZ.models.game.minigame.minigameCondition;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.LoseCondition;

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
