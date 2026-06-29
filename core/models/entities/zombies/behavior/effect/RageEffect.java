package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;

public class RageEffect extends Effect {
    private final float speedMultiplier;
    private final int extraEatDps; // for increase the power of eating plant

    public RageEffect(Zombie zombie, float speedMultiplier, int extraEatDps) {
        super(zombie, -1);
        this.speedMultiplier = speedMultiplier;
        this.extraEatDps = extraEatDps;
    }

    @Override
    public void onApply() {
        zombie.applySpeedMultiplier(speedMultiplier); // increase speed
        //increase dps
    }

    @Override
    public void onRemove() {
        zombie.resetSpeed();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
