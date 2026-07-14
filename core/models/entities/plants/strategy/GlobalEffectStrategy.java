package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Global Effect Strategy:
 * Triggers an immediate, board-wide effect as soon as the plant is placed.
 * After the effect is applied, the plant typically disappears or dies shortly after.
 */

public class GlobalEffectStrategy implements IPlantStrategy {
    private static final int ACTIVATION_DELAY = TimeManager.TICKS_PER_SECOND;
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= ACTIVATION_DELAY) {

            if (context.getName().equals("Ice-shroom")) {
                System.out.println("❄️ Ice-shroom exploded and froze the entire board!");

                for (Zombie z : GameSession.getInstance().getArena().getActiveZombies()) {
                    if (!z.isDead()) {
                        // freeze zombie
                        System.out.println("-> " + z.getName() + " is completely frozen!");
                    }
                }
            }

            context.takeDamage(context.getCurrentHp());
        }
    }
}
