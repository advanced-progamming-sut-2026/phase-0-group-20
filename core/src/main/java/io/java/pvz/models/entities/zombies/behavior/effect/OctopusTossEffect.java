package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class OctopusTossEffect extends Effect {
    private enum Phase { IDLE, TOSS }

    private final int tossIntervalTicks;
    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;
    private int tossAnimationTicks;
    private int tossFrameTick;
    private boolean hasTossed;

    public OctopusTossEffect(Zombie zombie, int tossIntervalSeconds) {
        super(zombie, -1);
        this.tossIntervalTicks = tossIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.IDLE;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tDur = (anim != null && anim.hasClip("toss")) ? anim.getDuration("toss") : 3.0667f;

        this.tossAnimationTicks = (int) (tDur * TimeManager.TICKS_PER_SECOND);
        this.tossFrameTick = (int) (tossAnimationTicks * 0.5f);
    }

    @Override
    public void onApply() {
        this.currentPhase = Phase.IDLE;
        this.intervalTicksCounter = 0;
        this.phaseTicksCounter = 0;
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        switch (currentPhase) {
            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= tossIntervalTicks) {
                    if (findTargetPlant() != null) {
                        currentPhase = Phase.TOSS;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        hasTossed = false;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.TOSS);
                        zombie.applySpeedMultiplier(0f);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case TOSS:
                phaseTicksCounter++;

                if (phaseTicksCounter == tossFrameTick && !hasTossed) {
                    tossOctopus();
                    hasTossed = true;
                }

                if (phaseTicksCounter >= tossAnimationTicks) {
                    currentPhase = Phase.IDLE;
                    phaseTicksCounter = 0;

                    if (!zombie.isDead()) {
                        zombie.setState(ZombieState.WALKING);
                        zombie.resetSpeed();
                    }
                }
                break;
        }
    }

    private Plant findTargetPlant() {
        GameSession session = GameSession.getInstance();
        Plant nearestPlant = null;
        int closestCol = -1;

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile().getRow() == zombie.getRow()
                && p.getPlacedTile().getCol() <= zombie.getCol()) {
                if (p.getPlacedTile().getCol() > closestCol) {
                    if (!p.isFrozen() && !p.hasOctopus()) {
                        closestCol = p.getPlacedTile().getCol();
                        nearestPlant = p;
                    }
                }
            }
        }
        return nearestPlant;
    }

    private void tossOctopus() {
        Plant target = findTargetPlant();
        if (target != null) {
            target.receiveOctopus();
            notify("Octopus Zombie threw an octopus on " + target.getName() + "!");
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!zombie.isDead()) {
            zombie.resetSpeed();
            zombie.setState(ZombieState.WALKING);
        }
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
