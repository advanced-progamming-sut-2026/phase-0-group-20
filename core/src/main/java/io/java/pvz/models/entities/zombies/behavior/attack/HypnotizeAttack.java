package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.timeManager.TimeManager;

public class HypnotizeAttack implements AttackBehavior {
    private final Zombie zombie;
    private int smashCooldownTimer = 0;
    private static final int SMASH_COOLDOWN_TICKS = (int) (1.5f * TimeManager.TICKS_PER_SECOND);

    public HypnotizeAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Zombie targetZombie = zombie.getTargetZombie();

        if (targetZombie != null && !targetZombie.isDead()) {
            if (zombie.getSmashDamage() > 0) {
                smashCooldownTimer++;
                if (smashCooldownTimer >= SMASH_COOLDOWN_TICKS) {
                    int smashDamage = zombie.getSmashDamage();
                    targetZombie.takeDamage(smashDamage);
                    smashCooldownTimer = 0;
                }

            } else {
                int damage = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;
                targetZombie.takeDamage(damage * 2);
            }
        }
    }
}
