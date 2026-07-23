package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class SplashEffect implements ProjectileEffect {
    private static final double SPLASH_RADIUS = 1.5;
    private final int splashDamage;

    public SplashEffect(int splashDamage) {
        this.splashDamage = splashDamage;
    }

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {

        List<Zombie> nearbyZombies = GameSession.getInstance().getArena().getZombiesInRadius(
                zombie.getCol(), zombie.getRow(), SPLASH_RADIUS
        );

        for (Zombie z : nearbyZombies) {
            if (z != zombie && !z.isDead()) {
                z.takeDamage(splashDamage, projectile);
                if (z.isDead()) {
                    projectile.getPlant().onZombieDeath(z);
                }
            }
        }

        notify("🍉 Splash damage applied to " + nearbyZombies.size() + " nearby zombies!");
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
