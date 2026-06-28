package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class OctopusAttack implements AttackBehavior {
    private final Zombie zombie;

    public OctopusAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : Logic
    }
}
