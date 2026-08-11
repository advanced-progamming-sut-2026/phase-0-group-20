package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class DodoFlyEffect extends Effect {
    private enum FlyPhase { START, LOOP, END }

    private FlyPhase currentPhase;
    private int phaseTicksCounter;

    private final int startTicks;
    private final int loopTicks;
    private final int endTicks;

    private final float startX;
    private final float targetX;

    public DodoFlyEffect(Zombie zombie, int targetCol) {
        super(zombie, -1);
        this.currentPhase = FlyPhase.START;
        this.phaseTicksCounter = 0;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tStart = (anim != null && anim.hasClip("fly_start")) ? anim.getDuration("fly_start") : 0.9667f;
        float tLoop = (anim != null && anim.hasClip("fly_loop")) ? anim.getDuration("fly_loop") : 2.6667f;
        float tEnd = (anim != null && anim.hasClip("fly_end")) ? anim.getDuration("fly_end") : 1.5f;

        this.startTicks = (int) (tStart * TimeManager.TICKS_PER_SECOND);
        this.loopTicks = (int) (tLoop * TimeManager.TICKS_PER_SECOND);
        this.endTicks = (int) (tEnd * TimeManager.TICKS_PER_SECOND);

        this.startX = zombie.getX();

        this.targetX = (targetCol * PhysicalConstants.TILE_WIDTH) + PhysicalConstants.GRID_START_X + (PhysicalConstants.TILE_WIDTH / 2f);
    }

    @Override
    public void onApply() {
        zombie.setAttacking(false);
        zombie.applySpeedMultiplier(0f);
        zombie.setState(ZombieState.FLY_START);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        phaseTicksCounter++;

        switch (currentPhase) {
            case START:
                if (phaseTicksCounter >= startTicks) {
                    currentPhase = FlyPhase.LOOP;
                    zombie.setState(ZombieState.FLYING);
                    phaseTicksCounter = 0;
                }
                break;

            case LOOP:
                float progress = (float) phaseTicksCounter / loopTicks;
                float currentX = startX + ((targetX - startX) * progress);

                zombie.setX(currentX);

                if (phaseTicksCounter >= loopTicks) {
                    currentPhase = FlyPhase.END;
                    zombie.setState(ZombieState.FLY_END);
                    phaseTicksCounter = 0;
                }
                break;

            case END:
                if (phaseTicksCounter >= endTicks) {
                    onRemove();
                }
                break;
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!zombie.isDead()) {
            zombie.setState(ZombieState.WALKING);
            zombie.resetSpeed();
        }
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
