package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public class SunAbsorber extends Effect {
    private final int stealIntervalTicks; // interval for steal sun
    private final int sunAmount; // amount of sun
    private boolean isAbsorbing;

    public SunAbsorber(Zombie zombie, int stealIntervalTicks, int sunAmount) {
        super(zombie, -1);
        this.stealIntervalTicks = stealIntervalTicks;
        this.sunAmount = sunAmount;
        this.isAbsorbing = true;
    }

    @Override
    public void onApply() {
        // animation
    }

    @Override
    public void execute() {
        if (!isAbsorbing) return;

        super.execute();

        if (currentTick % stealIntervalTicks == 0) {
            stealSunFromPlayer();
        }
    }

    private void stealSunFromPlayer() {
        // decrease the amount of sun
    }

    @Override
    public void onRemove() {
        // dead
    }

    @Override
    public boolean isFinished() {
        return !isAbsorbing;
    }

    public void stopAbsorbing() {
        this.isAbsorbing = false;
    }
}
