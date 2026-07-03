package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.game.Arena;
import models.game.GameSession;

public class IceEffect implements ProjectileEffect {

    private static final int CHILL_DURATION_TICKS = 100;

    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        zombie.addEffect(new ChillEffect(zombie, CHILL_DURATION_TICKS));
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
