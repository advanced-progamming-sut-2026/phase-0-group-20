package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.PoisonEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.timeManager.TimeManager;

public class PoisonProjectileEffect implements ProjectileEffect {

    private static final int POISON_DURATION_TICKS = 5 * TimeManager.TICKS_PER_SECOND;
    private static final int POISON_DPS = 10;

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        for (ZombieEffect effect : zombie.getActiveEffects()) {
            if (effect instanceof PoisonEffect) return;
        }
        zombie.addEffect(new PoisonEffect(
                zombie, POISON_DURATION_TICKS, POISON_DPS));
    }

    @Override
    public int getDamageMultiplier() {
        return 1;
    }

    @Override
    public boolean ignoresArmor() {
        return true;
    }

    @Override
    public boolean meltsIce() {
        return false;
    }
}
