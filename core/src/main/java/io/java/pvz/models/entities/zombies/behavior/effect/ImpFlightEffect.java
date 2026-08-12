package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.timeManager.TimeManager;

public class ImpFlightEffect extends Effect {
    private int ticksCounter = 0;
    private final int flightTicks = (int) (0.8f * TimeManager.TICKS_PER_SECOND);
    private final int landTicks = (int) (1.0f * TimeManager.TICKS_PER_SECOND);
    private boolean hasLanded = false;

    public ImpFlightEffect(Zombie imp) {
        super(imp, -1);
    }

    @Override
    public void onApply() {
        zombie.setState(ZombieState.FLYING);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;
        ticksCounter++;

        if (ticksCounter >= flightTicks && !hasLanded) {
            zombie.setState(ZombieState.LANDING);
            hasLanded = true;
            ticksCounter = 0;
        } else if (hasLanded && ticksCounter >= landTicks) {
            zombie.setState(ZombieState.WALKING);
            zombie.resetSpeed();
            zombie.getActiveEffects().remove(this);
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }
    @Override
    public void onRemove() { }
    @Override
    public boolean isFinished() { return zombie.isDead(); }
}
