package models.game.minigame.minigameCondition;

import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.game.LoseCondition;

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
