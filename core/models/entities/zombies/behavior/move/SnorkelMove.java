package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class SnorkelMove implements MoveBehavior {

    private final Zombie zombie;
    private boolean isSubmerged;

    public SnorkelMove(Zombie zombie) {
        this.zombie = zombie;
        this.isSubmerged = true;
    }

    @Override
    public void execute() {
        zombie.moveForward(); // under water
    }

    public void setSubmerged(boolean submerged) {
        this.isSubmerged = submerged;
    }

    public boolean isSubmerged() {
        return isSubmerged;
    }
}
