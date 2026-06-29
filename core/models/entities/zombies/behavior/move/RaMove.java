package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class RaMove implements MoveBehavior {
    private final Zombie zombie;
    private boolean isStealingSun;

    public RaMove(Zombie zombie) {
        this.zombie = zombie;
        this.isStealingSun = false;
    }

    @Override
    public void execute() {
        if (!isStealingSun) {
            zombie.moveForward();
        }
    }

    public void setStealingSun(boolean stealing) { // it will call in stealing sun logic
        this.isStealingSun = stealing;
    }
}
