package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public class FreezeEffect extends Effect {

    public FreezeEffect(Zombie zombie, int durationTicks) {
        super(zombie, durationTicks);
    }

    @Override
    public void onApply() {
        zombie.applySpeedMultiplier(0f); // fully stop
    }

    @Override
    public void onRemove() {
        zombie.resetSpeed(); // back to ordinary speed
    }
}
