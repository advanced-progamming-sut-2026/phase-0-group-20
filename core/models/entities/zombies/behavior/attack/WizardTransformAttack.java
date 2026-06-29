package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class WizardTransformAttack implements AttackBehavior {
    private final Zombie zombie;

    public WizardTransformAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : sheep
    }
}
