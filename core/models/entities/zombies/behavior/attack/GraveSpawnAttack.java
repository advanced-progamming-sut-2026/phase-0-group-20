package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class GraveSpawnAttack implements AttackBehavior {
    private final Zombie zombie;

    public GraveSpawnAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : find empty cell and spawn a grave
    }
}
