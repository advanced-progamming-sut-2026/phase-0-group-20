package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class TurquoiseMove implements MoveBehavior {
    private final Zombie zombie;
    private boolean isChanneling; // absorb sun

    public TurquoiseMove(Zombie zombie) {
        this.zombie = zombie;
        this.isChanneling = false;
    }

    @Override
    public void execute() {
        if (!isChanneling) {
            zombie.moveForward();
        }
    }

    public boolean isChanneling() {
        return isChanneling;
    }

    public void setChanneling(boolean channeling) { // call in attack behavior
        this.isChanneling = channeling;
    }
}
