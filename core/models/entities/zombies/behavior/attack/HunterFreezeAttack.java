package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class HunterFreezeAttack implements AttackBehavior {
    private final Zombie zombie;

    public HunterFreezeAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : Logic for this
    }
}
