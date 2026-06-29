package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class FishermanHookAttack implements AttackBehavior {

    private final Zombie zombie;

    public FishermanHookAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : Logic
    }
}
