package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public class FireEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, Arena board, Projectile projectile) {
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
