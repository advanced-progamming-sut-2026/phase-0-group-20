package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

public class LightningEffect implements ProjectileEffect {
    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        System.out.println("⚡ " + zombie.getName() + " HAS BEEN ZAPPED AND REDUCED TO ASHES! ⚡");
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
