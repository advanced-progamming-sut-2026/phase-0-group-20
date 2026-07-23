package models.entities.zombies.behavior.context;

public class JugglerContext {
    private int spinTimer = 0;
    private static final int SPIN_DURATION = 20;

    public void triggerSpin() {
        this.spinTimer = SPIN_DURATION;
    }

    public void tickTimer() {
        if (spinTimer > 0) {
            spinTimer--;
        }
    }

    public boolean isSpinning() {
        return spinTimer > 0;
    }
}
