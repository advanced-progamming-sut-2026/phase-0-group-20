package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public abstract class Effect implements ZombieEffect {
    protected final Zombie zombie;
    protected int durationTicks;
    protected int currentTick;

    public Effect(Zombie zombie, int durationTicks) {
        this.zombie = zombie;
        this.durationTicks = durationTicks;
        this.currentTick = 0;
    }

    public abstract void onApply();

    public abstract void onRemove();

    @Override
    public void execute() {
        if (currentTick == 0) {
            onApply();
        }

        currentTick++;

        if (isFinished()) {
            onRemove();
        }
    }

    public boolean isFinished() {
        return currentTick >= durationTicks;
    }
}
