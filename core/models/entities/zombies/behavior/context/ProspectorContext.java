package models.entities.zombies.behavior.context;

import models.game.GameSession;

public class ProspectorContext {
    private int dynamiteTimer = 0;
    private final int EXPLOSION_TIME = 100;
    private boolean isDynamiteLit = true;
    private boolean isMovingReverse = false;

    public void tickTimer() {
        if (isDynamiteLit && dynamiteTimer < EXPLOSION_TIME) {
            dynamiteTimer++;
        }
    }

    public boolean shouldExplode() {
        return isDynamiteLit && dynamiteTimer >= EXPLOSION_TIME;
    }

    public void extinguishDynamite() {
        isDynamiteLit = false;
        GameSession.notify("Prospector's dynamite was extinguished by ice!");
    }

    public void triggerJump() {
        isDynamiteLit = false;
        isMovingReverse = true;
    }

    public boolean isDynamiteLit() {
        return isDynamiteLit;
    }

    public boolean isMovingReverse() {
        return isMovingReverse;
    }
}
