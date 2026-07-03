package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.PoisonEffect;
import models.game.Arena;
import models.game.GameSession;

public class PoisonProjectileEffect implements ProjectileEffect {

    private static final int POISON_DURATION_TICKS = 50;
    private static final int POISON_DPS = 10;

    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        zombie.addEffect(new PoisonEffect(
                zombie, POISON_DURATION_TICKS, POISON_DPS));
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
