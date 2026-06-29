package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class PeriodicActionMove implements MoveBehavior {

    private final Zombie zombie;
    private final int tickInterval;
    private final boolean movesForward;
    private final Runnable action;
    private int tickCounter;

    public PeriodicActionMove(Zombie zombie, int tickInterval, boolean movesForward, Runnable action) {
        this.zombie = zombie;
        this.tickInterval = tickInterval;
        this.movesForward = movesForward;
        this.action = action;
        this.tickCounter = 0;
    }

    @Override
    public void execute() {
        if (movesForward) {
            zombie.moveForward();
        }

        tickCounter++;
        if (tickCounter >= tickInterval) {
            action.run();
            tickCounter = 0;
        }
    }
}
