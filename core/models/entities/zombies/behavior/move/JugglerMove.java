package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class JugglerMove implements MoveBehavior {
    private static final int SPIN_COOLDOWN_TICKS = 20;
    private final Zombie zombie;
    private final float spinSpeedMultiplier = 2.5f;
    private boolean isSpinning;
    private int ticksSinceLastProjectile;

    public JugglerMove(Zombie zombie) {
        this.zombie = zombie;
        this.isSpinning = false;
        this.ticksSinceLastProjectile = 0;
    }

    @Override
    public void execute() {
        zombie.moveForward();

        if (isSpinning) {
            ticksSinceLastProjectile++;
            if (ticksSinceLastProjectile >= SPIN_COOLDOWN_TICKS) {
                stopSpinning();
            }
        }
    }

    public void onProjectileIncoming() {
        ticksSinceLastProjectile = 0;
        startSpinning();
    }

    public void startSpinning() {
        if (!isSpinning) {
            isSpinning = true;
            zombie.applySpeedMultiplier(spinSpeedMultiplier);
        }
    }

    public void stopSpinning() {
        if (isSpinning) {
            isSpinning = false;
            zombie.resetSpeed();
        }
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
