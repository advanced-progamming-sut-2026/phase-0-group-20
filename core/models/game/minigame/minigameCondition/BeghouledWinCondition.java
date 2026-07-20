package models.game.minigame.minigameCondition;

import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.game.WinCondition;
import models.game.minigame.BeghouledLevel;

public class BeghouledWinCondition implements WinCondition {
    @Override
    public boolean isWon(GameSession session) {
        if (session.getCurrentMode() instanceof BeghouledLevel) {
            BeghouledLevel beghouledLevel = (BeghouledLevel) session.getCurrentMode();


            if (beghouledLevel.getSuccessfulMatches() >= beghouledLevel.getTargetMatches()) {

                if (!session.getArena().getActiveZombies().isEmpty()) {
                    for (Zombie zombie : session.getArena().getActiveZombies()) {
                        zombie.takeDirectDamage(100000);
                        session.getTimeManager().unregisterTicker(zombie);
                    }
                    session.getArena().getActiveZombies().clear();

                    notify("Target matches reached! All zombies in the arena have been destroyed.");
                }

                return true;
            }
        }
        return false;
    }
}
