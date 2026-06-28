package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class NormalAttack implements AttackBehavior {
    private final Zombie zombie;

    public NormalAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        int damagePerTick = zombie.getEatDps() / 10;

        // TODO: logic for eating plant
    }
}
