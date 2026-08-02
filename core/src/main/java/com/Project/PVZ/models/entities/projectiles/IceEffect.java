package com.Project.PVZ.models.entities.projectiles;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.behavior.effect.FreezeEffect;
import com.Project.PVZ.models.entities.zombies.behavior.effect.ZombieEffect;

public class IceEffect implements ProjectileEffect {

    private static final int FREEZE_DURATION_TICKS = 30;

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
