package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

public interface ProjectileEffect {
    void applyEffect(GameSession gameSession);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();
}
