package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class JugglerMove implements MoveBehavior {
    private final Zombie zombie;
    private final float spinSpeedMultiplier = 2.5f;
    private boolean isSpinning;

    public JugglerMove(Zombie zombie) {
        this.zombie = zombie;
        this.isSpinning = false;
    }

    @Override
    public void execute() {
        zombie.moveForward();
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
