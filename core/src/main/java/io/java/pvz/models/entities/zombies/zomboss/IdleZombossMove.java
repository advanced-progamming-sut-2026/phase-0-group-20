package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class IdleZombossMove implements IZombossMove {
    private final Zomboss zomboss;
    private final IZombossMove switchLaneMove;
    private int cooldownTicks;
    private final Random random = new Random();

    public IdleZombossMove(Zomboss zomboss, IZombossMove switchLaneMove) {
        this.zomboss = zomboss;
        this.switchLaneMove = switchLaneMove;
    }

    @Override
    public void onEnter() {
        this.cooldownTicks = (10 + random.nextInt(6)) * TimeManager.TICKS_PER_SECOND;
    }

    @Override
    public void execute() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        } else {
            this.onExit();
            switchLaneMove.reset();
            switchLaneMove.onEnter();
            zomboss.setMoveBehavior(switchLaneMove);
        }
    }

    @Override
    public void onExit() {
    }

    @Override
    public void reset() {
        this.cooldownTicks = 0;
    }
}
