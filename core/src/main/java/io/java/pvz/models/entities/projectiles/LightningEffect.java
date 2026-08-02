package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.entities.zombies.Zombie;

public class LightningEffect implements ProjectileEffect {
    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {


        notify("⚡ " + zombie.getName() + " HAS BEEN ZAPPED AND REDUCED TO ASHES! ⚡");
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
