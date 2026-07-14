package models.entities.projectiles;

import models.entities.zombies.Zombie;

public class FireEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        zombie.removeChillEffect();
        zombie.removeFreezeEffect();
    }

    @Override
    public int getDamageMultiplier() {
        return 2;
    }

    @Override
    public boolean ignoresArmor() {
        return false;
    }

    @Override
    public boolean meltsIce() {
        return true;
    }
}
