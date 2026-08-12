package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;

public class StationaryMove implements MoveBehavior {
    private final Zombie zombie;

    public StationaryMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {

    }
}
