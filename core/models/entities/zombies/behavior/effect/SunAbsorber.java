package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.Random;

public class SunAbsorber extends Effect {
    private final int stealIntervalTicks; // interval for steal sun
    private final int sunAmount; // amount of sun
    private boolean isAbsorbing;
    private final Random random = new Random();

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
                isAbsorbing = true;
                absorbingTicksCounter = 0;
                intervalTicksCounter = 0;
                notify("Ra zombie in (" + zombie.getCol() + ", " + zombie.getRow() + ") started absorbing...");
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
        if (GameSession.getInstance().getArena().getActiveSuns().isEmpty()) {
            return;
        }

        int total = GameSession.getInstance().getArena().getActiveSuns().size();
        GameSession.getInstance().getArena().getActiveSuns().remove(random.nextInt(total));
        GameSession.getInstance().useSun(sunAmount);

        notify("Ra zombie in (" + zombie.getCol() + ", " + zombie.getRow() + ") stole a sun!");
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
