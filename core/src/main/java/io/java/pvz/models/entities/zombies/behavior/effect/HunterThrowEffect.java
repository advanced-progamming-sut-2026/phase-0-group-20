package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class HunterThrowEffect extends Effect {
    private enum Phase { IDLE, THROW }

    private final int throwIntervalTicks;
    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;
    private int throwAnimationTicks;
    private int shootFrameTick;
    private boolean hasShot;

    public HunterThrowEffect(Zombie zombie, int throwIntervalSeconds) {
        super(zombie, -1);
        this.throwIntervalTicks = throwIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.IDLE;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tDur = (anim != null && anim.hasClip("throw")) ? anim.getDuration("throw") : 2.1f;

        this.throwAnimationTicks = (int) (tDur * TimeManager.TICKS_PER_SECOND);

        this.shootFrameTick = (int) (throwAnimationTicks * 0.6f);
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
                if (intervalTicksCounter >= throwIntervalTicks) {
                    if (findTargetPlant() != null) {
                        currentPhase = Phase.THROW;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        hasShot = false;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.THROW);
                        zombie.applySpeedMultiplier(0f);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case THROW:
                phaseTicksCounter++;

                if (phaseTicksCounter == shootFrameTick && !hasShot) {
                    shootIce();
                    hasShot = true;
                }

                if (phaseTicksCounter >= throwAnimationTicks) {
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
            if (p.getPlacedTile().getRow() == zombie.getRow() && p.getPlacedTile().getCol() <= zombie.getCol()) {
                if (p.getPlacedTile().getCol() > closestCol && !p.isFrozen()) {
                    closestCol = p.getPlacedTile().getCol();
                    nearestPlant = p;
                }
            }
        }
        return nearestPlant;
    }

    private void shootIce() {
        Plant target = findTargetPlant();
        if (target != null) {
            target.receiveIceHit();

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("HUNTER_SNOWBALL_HIT")
                    .coordinate(target.getPlacedTile().getRow(), target.getPlacedTile().getCol())
                    .build());
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
