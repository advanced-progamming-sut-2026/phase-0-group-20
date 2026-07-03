package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

public class NormalEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
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
