package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

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
        if (currentTick % 10 == 0) { // each 1 sec
            zombie.takeDamage(dps);
        }
    }

    @Override
    public void onRemove() {
        // back to main state
    }
}
