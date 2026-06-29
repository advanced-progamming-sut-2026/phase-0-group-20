package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class PushMove implements MoveBehavior {

    private final Zombie zombie;

    public PushMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        zombie.moveForward();

        // TODO : we will add arcade as a new entity and move arcade here
    }
}
