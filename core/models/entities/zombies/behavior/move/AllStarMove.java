package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class AllStarMove implements MoveBehavior {
    private final Zombie zombie;
    private final float runSpeedMultiplier = 3.0f; // it can be different
    private boolean isRunning;


    public AllStarMove(Zombie zombie) {
        this.zombie = zombie;
        this.isRunning = true;

        zombie.applySpeedMultiplier(runSpeedMultiplier);
    }


    @Override
    public void execute() {
        zombie.moveForward();
    }

    public void stopRunning() { // call in attack behavior for AllStar zombie
        if (isRunning) {
            isRunning = false;
            zombie.resetSpeed(); // normal speed
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
