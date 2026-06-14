package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public interface ProjectileEffect {
    void applyEffect(Zombie zombie, Arena board);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();
}
