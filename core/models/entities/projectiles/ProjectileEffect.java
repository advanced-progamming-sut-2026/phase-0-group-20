package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public interface ProjectileEffect {

    void applyEffect(Zombie zombie, Arena board, Projectile projectile);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();
}
