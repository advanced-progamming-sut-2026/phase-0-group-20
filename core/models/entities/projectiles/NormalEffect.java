package models.entities.projectiles;

import models.entities.zombies.Zombie;

public class NormalEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        // no effect
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
