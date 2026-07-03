package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public class ChillEffect extends Effect {
    private final float speedMultiplier;

    public ChillEffect(Zombie zombie, int durationTicks) {
        this(zombie, durationTicks, 0.5f);
    }

    public ChillEffect(Zombie zombie, int durationTicks, float speedMultiplier) {
        super(zombie, durationTicks);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void onApply() {
        zombie.applySpeedMultiplier(speedMultiplier);
    }

    @Override
    public void onRemove() {
        zombie.resetSpeed();
    }
}
