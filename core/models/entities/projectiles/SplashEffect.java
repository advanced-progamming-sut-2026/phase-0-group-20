package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class SplashEffect implements ProjectileEffect {
    private static final double SPLASH_RADIUS = 1.5;

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        int splashDamage = projectile.getDamage() / 2;

        List<Zombie> nearbyZombies = GameSession.getInstance().getArena().getZombiesInRadius(
                zombie.getCol(), zombie.getRow(), SPLASH_RADIUS
        );

        for (Zombie z : nearbyZombies) {
            if (z != zombie && !z.isDead()) {
                z.takeDamage(splashDamage, projectile);
            }
        }

        System.out.println("🍉 Splash damage applied to " + (nearbyZombies.size() - 1) + " nearby zombies!");
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
