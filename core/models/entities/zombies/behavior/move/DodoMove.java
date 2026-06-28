package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class DodoMove implements MoveBehavior{
    private final Zombie zombie;
    private boolean isFlying;
    private int flyTicks;
    private final int FLY_DURATION_TICKS = 45; // it will change

    public DodoMove(Zombie zombie) {
        this.zombie = zombie;
        this.isFlying = false;
        this.flyTicks = 0;
    }

    @Override
    public void execute() {
        if (isFlying) {
            zombie.moveForward(); // it can fly with another speed
            flyTicks++;

            if (flyTicks >= FLY_DURATION_TICKS) {
                isFlying = false;
            }
        } else {
            zombie.moveForward();

            // TODO: add logic for fly and call (star flying)
        }
    }

    public void startFlying() {
        if (!isFlying) {
            isFlying = true;
            flyTicks = 0;
        }
    }

    public boolean isFlying() {
        return isFlying;
    }
}
