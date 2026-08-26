package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.entities.zombies.Zombie;

public class HypnotizeEffect implements ProjectileEffect {
    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        zombie.hypnotize();
    }

    @Override
    public int getDamageMultiplier() {
        return 0;
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
