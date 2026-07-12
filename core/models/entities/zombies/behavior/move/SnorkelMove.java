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
        boolean hasPlantToEat = zombie.getTile() != null && !zombie.getTile().getPlants().isEmpty();
        isSubmerged = !hasPlantToEat;

        zombie.moveForward();  // under water
    }

    public boolean isSubmerged() {
        return isSubmerged;
    }

    public void setSubmerged(boolean submerged) {
        this.isSubmerged = submerged;
    }
}
