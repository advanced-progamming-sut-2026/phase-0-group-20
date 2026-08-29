package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SunAbsorber extends Effect {
    private enum AbsorbPhase {IDLE, POWER_UP, POWER, POWER_DOWN}

    private final int stealIntervalTicks;
    private final Random random = new Random();

    private AbsorbPhase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;

    private Sun targetedSun = null;
    private float startSunX, startSunY;
    private float targetZombieX, targetZombieY;

    private int powerUpTicks;
    private int powerTicks;
    private int powerDownTicks;
    private int sunAmount;


    public SunAbsorber(Zombie zombie, int stealIntervalTicks) {
        super(zombie, -1);
        this.stealIntervalTicks = stealIntervalTicks;
        this.currentPhase = AbsorbPhase.IDLE;
        this.intervalTicksCounter = 0;
        this.phaseTicksCounter = 0;
        this.sunAmount = 0;
        loadAnimationDurations();
    }

    private void loadAnimationDurations() {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());

        float pUp = (anim != null && anim.hasClip("power_up")) ? anim.getDuration("power_up") : 0.6667f;
        float pLoop = (anim != null && anim.hasClip("power")) ? anim.getDuration("power") : 1.0f;
        float pDown = (anim != null && anim.hasClip("power_down")) ? anim.getDuration("power_down") : 1.2667f;

        this.powerUpTicks = (int) (pUp * TimeManager.TICKS_PER_SECOND);
        this.powerTicks = (int) (pLoop * TimeManager.TICKS_PER_SECOND);
        this.powerDownTicks = (int) (pDown * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void onApply() {
        this.currentPhase = AbsorbPhase.IDLE;
        this.intervalTicksCounter = 0;
        this.phaseTicksCounter = 0;
        this.targetedSun = null;
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;
        switch (currentPhase) {
            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= stealIntervalTicks) {
                    if (canStealSomething()) {
                        currentPhase = AbsorbPhase.POWER_UP;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.POWER_UP);
                        zombie.applySpeedMultiplier(0f);
                    } else intervalTicksCounter = 0;
                }
                break;
            case POWER_UP:
                phaseTicksCounter++;
                if (phaseTicksCounter >= powerUpTicks) {
                    currentPhase = AbsorbPhase.POWER;
                    zombie.setState(ZombieState.POWER);
                    phaseTicksCounter = 0;
                    lockOnTargetSun();
                }
                break;
            case POWER:
                phaseTicksCounter++;
                if (targetedSun != null && !targetedSun.isCollected()) {
                    float progress = (float) phaseTicksCounter / powerTicks;
                    float newX = startSunX + (targetZombieX - startSunX) * progress;
                    float newY = startSunY + (targetZombieY - startSunY) * progress;
                    targetedSun.getPosition().setPosition(newX, newY);
                }
                if (phaseTicksCounter >= powerTicks) {
                    currentPhase = AbsorbPhase.POWER_DOWN;
                    zombie.setState(ZombieState.POWER_DOWN);
                    phaseTicksCounter = 0;
                    executeSteal();
                }
                break;
            case POWER_DOWN:
                phaseTicksCounter++;
                if (phaseTicksCounter >= powerDownTicks) stopAbsorbing();
                break;
        }
    }

    private boolean canStealSomething() {
        for (Sun s : GameSession.getInstance().getArena().getActiveSuns()) {
            if (!s.isCollected() && !s.isBeingAbsorbed()) {
                return true;
            }
        }
        return false;
    }

    private void lockOnTargetSun() {
        List<Sun> activeSuns = GameSession.getInstance().getArena().getActiveSuns();
        List<Sun> stealableSuns = new ArrayList<>();

        for (Sun s : activeSuns) {
            if (!s.isCollected() && !s.isBeingAbsorbed() && !s.getType().equals(SunType.RADIOACTIVE_SUN)) {
                stealableSuns.add(s);
            }
        }

        if (!stealableSuns.isEmpty()) {
            targetedSun = stealableSuns.get(random.nextInt(stealableSuns.size()));

            targetedSun.setBeingAbsorbed(true);
            targetedSun.setFalling(false);

            startSunX = targetedSun.getPosition().getX();
            startSunY = targetedSun.getPosition().getY();

            targetZombieX = zombie.getX() - 40f;
            targetZombieY = zombie.getY() + 110f;

            notify("Ra zombie locked on a sun at (" +
                (targetedSun.getCol() + 1) + "," + (targetedSun.getRow() + 1) + ")");
        } else {
            targetedSun = null;
        }
    }

    private void executeSteal() {
        if (targetedSun != null && !targetedSun.isCollected()) {
            GameSession.getInstance().getArena().getActiveSuns().remove(targetedSun);
            GameSession.getInstance().getTimeManager().unregisterTicker(targetedSun);
            sunAmount += targetedSun.getAmountProduced();
            targetedSun = null;
        }
    }

    @Override
    public float getRemainingSeconds() {
        return 0f;
    }

    @Override
    public void onRemove() {
        stopAbsorbing();
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }

    public void stopAbsorbing() {
        this.currentPhase = AbsorbPhase.IDLE;
        this.phaseTicksCounter = 0;

        if (targetedSun != null) {
            targetedSun.setBeingAbsorbed(false);
            targetedSun = null;
        }

        if (!zombie.isDead()) {
            zombie.setState(ZombieState.WALKING);
            zombie.resetSpeed();
        }
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public void setSunAmount(int sunAmount) {
        this.sunAmount = sunAmount;
    }
}
