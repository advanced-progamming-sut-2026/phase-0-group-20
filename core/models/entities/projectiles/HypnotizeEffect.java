package models.entities.projectiles;

import models.entities.zombies.Zombie;

public class HypnotizeEffect implements ProjectileEffect {
    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        notify("🌀 " + zombie.getName() + " HAS BEEN HYPNOTIZED! 🌀");
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
