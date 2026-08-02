package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.zombies.Zombie;

public class ProspectorAttack implements AttackBehavior {
    private final AttackBehavior normalAttack;

    public ProspectorAttack(Zombie zombie ) {
        this.normalAttack = new NormalAttack(zombie);
    }

    @Override
    public void execute() { // it's totally for graphic
        normalAttack.execute();
    }
}
