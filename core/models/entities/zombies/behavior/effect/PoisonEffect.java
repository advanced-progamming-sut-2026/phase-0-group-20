package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;
import models.timeManager.TimeManager;

public class PoisonEffect extends Effect {
    private final int dps; // damage per sec

    public PoisonEffect(Zombie zombie, int durationTicks, int dps) {
        super(zombie, durationTicks);
        this.dps = dps;
    }

    @Override
    public void onApply() {
        // change color of the zombie
    }

    @Override
    public void execute() {
        super.execute();

        if (currentTick % TimeManager.TICKS_PER_SECOND == 0) { // each 1 sec
            zombie.takeDamage(dps);
        }
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() || zombie.isDead();
    }
}
