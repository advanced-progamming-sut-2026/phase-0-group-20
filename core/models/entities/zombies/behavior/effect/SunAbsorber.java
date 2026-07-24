package models.entities.zombies.behavior.effect;

import models.entities.Sun;
import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class SunAbsorber extends Effect {
    private final int stealIntervalTicks; // interval for steal sun
    private final int sunAmount; // amount of sun
    private final Random random = new Random();
    private boolean isAbsorbing;
    private int absorbingTicksCounter;
    private int intervalTicksCounter;

    public SunAbsorber(Zombie zombie, int stealIntervalTicks, int sunAmount) {
        super(zombie, -1);
        this.stealIntervalTicks = stealIntervalTicks;
        this.sunAmount = sunAmount;
        this.isAbsorbing = false;
        this.intervalTicksCounter = 0;
        this.absorbingTicksCounter = 0;
    }

    @Override
    public void onApply() {
        this.isAbsorbing = false;
        this.intervalTicksCounter = 0;
        this.absorbingTicksCounter = 0;
    }

    @Override
    public void execute() {
        super.execute();

        if (isFinished()) {
            return;
        }

        if (!isAbsorbing) {
            intervalTicksCounter++;
            if (intervalTicksCounter >= stealIntervalTicks) {
                boolean hasSunOnGround = !GameSession.getInstance().getArena().getActiveSuns().isEmpty();
                boolean hasSunInBank = GameSession.getInstance().getCurrentSun() > 0;

                if (hasSunOnGround || hasSunInBank) {
                    isAbsorbing = true;
                    absorbingTicksCounter = 0;
                    intervalTicksCounter = 0;
                    notify("Ra zombie in (" + zombie.getCol() + ", " + zombie.getRow() + ") started absorbing...");
                } else {
                    intervalTicksCounter = 0;
                }
            }
        } else {
            absorbingTicksCounter++;

            if (absorbingTicksCounter >= 2 * TimeManager.TICKS_PER_SECOND) {
                stealSunFromPlayer();
                isAbsorbing = false;
            }
        }
    }

    @Override
    public float getRemainingSeconds() {
        if (isAbsorbing) {
            return (2 * TimeManager.TICKS_PER_SECOND - absorbingTicksCounter) / (float) TimeManager.TICKS_PER_SECOND;
        } else {
            return (stealIntervalTicks - intervalTicksCounter) / (float) TimeManager.TICKS_PER_SECOND;
        }
    }

    private void stealSunFromPlayer() {
        boolean stoleSomething = false;

        List<Sun> activeSuns = GameSession.getInstance().getArena().getActiveSuns();
        if (!activeSuns.isEmpty()) {
            int index = random.nextInt(activeSuns.size());
            Sun stolenSun = activeSuns.get(index);
            activeSuns.remove(index);

            GameSession.getInstance().getTimeManager().unregisterTicker(stolenSun);
            stoleSomething = true;
        }

        int currentBank = GameSession.getInstance().getCurrentSun();
        if (currentBank >= sunAmount) {
            GameSession.getInstance().useSun(sunAmount);
            stoleSomething = true;
        } else if (currentBank > 0) {
            GameSession.getInstance().useSun(currentBank);
            stoleSomething = true;
        }

        if (stoleSomething) {
            notify("Ra zombie in (" + zombie.getCol() + ", " + zombie.getRow() + ") stole a sun!");
        }
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
        this.isAbsorbing = false;
    }
}
