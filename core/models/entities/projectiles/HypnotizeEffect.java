package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

public class HypnotizeEffect implements ProjectileEffect {
    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        System.out.println("🌀 " + zombie.getName() + " HAS BEEN HYPNOTIZED! 🌀");
    }

    @Override
    public int getDamageMultiplier() {
        return 0;
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
