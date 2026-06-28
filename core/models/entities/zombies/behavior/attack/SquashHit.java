package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class SquashHit implements AttackBehavior {
    private final Zombie zombie;

    public SquashHit(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : Instant kill
    }
}
