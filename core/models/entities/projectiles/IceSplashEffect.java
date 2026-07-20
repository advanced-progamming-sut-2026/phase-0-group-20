package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class IceSplashEffect implements ProjectileEffect {
    private static final double SPLASH_RADIUS = 1.5;
    private final int splashDamage;

    public IceSplashEffect(int splashDamage) {
        this.splashDamage = splashDamage;
    }

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {

        List<Zombie> nearbyZombies = GameSession.getInstance().getArena().getZombiesInRadius(
                zombie.getCol(), zombie.getRow(), SPLASH_RADIUS
        );

        for (Zombie z : nearbyZombies) {
            if (z.isDead()) continue;

            // add effect for zombie

            if (z != zombie) {
                boolean killed = z.takeDamage(splashDamage);
                if (killed) {
                    projectile.getPlant().onZombieDeath(z);
                }
            }
        }

        notify("❄️ Ice Splash applied! " + nearbyZombies.size() + " zombies chilled.");
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
