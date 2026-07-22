package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public class ChillEffect extends Effect {
    private final float speedMultiplier;

    public ChillEffect(Zombie zombie, int durationTicks) {
        this(zombie, durationTicks, 0f); //fully stop
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
    public void execute() {
        super.execute();
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
        zombie.resetSpeed();
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() || zombie.isDead();
    }
}
