package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class LaserAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int laserDamage;

    public LaserAttack(Zombie zombie, int laserDamage) {
        this.zombie = zombie;
        this.laserDamage = laserDamage;
    }

    @Override
    public void execute() {
        // TODO : Logic
    }
}
