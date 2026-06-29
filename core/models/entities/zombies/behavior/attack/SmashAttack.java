package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class SmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int smashDamage;
    private int cooldownTicks;

    public SmashAttack(Zombie zombie, int smashDamage) {
        this.zombie = zombie;
        this.smashDamage = smashDamage;
        this.cooldownTicks = 0;
    }

    @Override
    public void execute() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }


        // TODO : execute a big damage to target plant
    }
}
