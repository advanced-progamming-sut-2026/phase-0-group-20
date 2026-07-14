package models.entities.projectiles;

import models.entities.zombies.Zombie;

public interface ProjectileEffect {

    void applyEffect(Zombie zombie, Projectile projectile);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();
}
