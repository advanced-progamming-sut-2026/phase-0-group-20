package io.java.pvz.models.entities.zombies.behavior.context;

import io.java.pvz.models.timeManager.TimeManager;

public class JugglerContext {
    public enum SpinPhase { NONE, SPIN_UP, SPINNING, SPIN_DOWN }

    private static final int SPIN_UP_TICKS   = Math.round(0.8667f * TimeManager.TICKS_PER_SECOND);
    private static final int SPIN_DOWN_TICKS = Math.round(0.5f    * TimeManager.TICKS_PER_SECOND);
    private static final int SPIN_DURATION   = 20;

    private SpinPhase phase = SpinPhase.NONE;
    private int timer = 0;

    public void triggerSpin() {
        if (phase == SpinPhase.NONE || phase == SpinPhase.SPIN_DOWN) {
            phase = SpinPhase.SPIN_UP;
            timer = SPIN_UP_TICKS;
        } else if (phase == SpinPhase.SPINNING) {
            timer = SPIN_DURATION;
        }
    }

    public void tickTimer() {
        if (phase == SpinPhase.NONE) return;
        timer--;
        if (timer <= 0) advancePhase();
    }

    private void advancePhase() {
        switch (phase) {
            case SPIN_UP   -> { phase = SpinPhase.SPINNING; timer = SPIN_DURATION; }
            case SPINNING  -> { phase = SpinPhase.SPIN_DOWN; timer = SPIN_DOWN_TICKS; }
            case SPIN_DOWN -> phase = SpinPhase.NONE;
            default -> {}
        }
    }

    public boolean isSpinning() {
        return phase == SpinPhase.SPINNING;
    }

    public SpinPhase getPhase() {
        return phase;
    }
}
