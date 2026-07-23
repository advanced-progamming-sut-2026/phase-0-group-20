package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;
import models.timeManager.TimeManager;

public class FreezeEffect extends Effect {

    private final static int DAMAGE_ICE = 2;

    public FreezeEffect(Zombie zombie, int durationTicks) {
        super(zombie, durationTicks);
    }

    @Override
    public void onApply() {
        zombie.applySpeedMultiplier(0.2f);
    }

    @Override
    public void execute() {
        super.execute();

        if (currentTick % TimeManager.TICKS_PER_SECOND == 0) {
            zombie.takeDamage(DAMAGE_ICE);
        }
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
        zombie.resetSpeed(); // back to ordinary speed
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() || zombie.isDead();
    }
}
