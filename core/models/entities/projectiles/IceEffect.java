package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.entities.zombies.behavior.effect.FreezeEffect;

public class IceEffect implements ProjectileEffect {

    private static final int FREEZE_DURATION_TICKS = 30;

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        zombie.addEffect(new FreezeEffect(zombie, FREEZE_DURATION_TICKS));
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
