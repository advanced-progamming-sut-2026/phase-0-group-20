package com.Project.PVZ.models.entities.zombies.behavior.move;

import com.Project.PVZ.models.entities.zombies.Zombie;

public class NormalMove implements MoveBehavior {
    private final Zombie zombie;

    public NormalMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        zombie.moveForward();
    }
}
