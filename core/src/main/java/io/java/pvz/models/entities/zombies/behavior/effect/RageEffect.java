package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class RageEffect extends Effect {
    private final float speedMultiplier;
    private final int extraEatDps;

    private boolean hasFinishedEnragingAnim = false;
    private int ticksCounter = 0;
    private final int enrageAnimTicks;

    public RageEffect(Zombie zombie, float speedMultiplier, int extraEatDps) {
        super(zombie, -1);
        this.speedMultiplier = speedMultiplier;
        this.extraEatDps = extraEatDps;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float animDuration = (anim != null && anim.hasClip("newspaper_defeat")) ? anim.getDuration("newspaper_defeat") : 1.4f;
        this.enrageAnimTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void onApply() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.ENRAGING);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        if (!hasFinishedEnragingAnim) {
            ticksCounter++;

            if (ticksCounter >= enrageAnimTicks) {
                hasFinishedEnragingAnim = true;

                zombie.setState(ZombieState.WALKING);
                zombie.resetSpeed();
                zombie.applySpeedMultiplier(speedMultiplier);
                zombie.applyEatSpeedMultiplier(extraEatDps);
            }
        }
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
        zombie.resetSpeed();
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
