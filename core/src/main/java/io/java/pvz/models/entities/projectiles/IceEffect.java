package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.timeManager.TimeManager;

public class IceEffect implements ProjectileEffect {

    private static final int FREEZE_DURATION_TICKS = 3 * TimeManager.TICKS_PER_SECOND;

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        for (ZombieEffect effect : zombie.getActiveEffects()) {
            if (effect instanceof FreezeEffect) return;
        }
        zombie.addEffect(new FreezeEffect(zombie, FREEZE_DURATION_TICKS));
    }

    @Override
    public int getDamageMultiplier() {
        return 1;
    }

    @Override
    public boolean ignoresArmor() {
        return false;
    }

    @Override
    public boolean meltsIce() {
        return false;
    }
}
