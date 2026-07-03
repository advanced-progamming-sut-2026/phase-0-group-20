package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

public class FireEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        zombie.removeChillEffect();
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
