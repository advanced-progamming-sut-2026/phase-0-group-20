package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class KingAttack implements AttackBehavior {
    private final Zombie zombie;

    public KingAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        // TODO : find near zombie with no armor and give them a darkArmor
    }
}
