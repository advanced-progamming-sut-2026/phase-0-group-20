package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

public interface ProjectileEffect {

    void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();
}
