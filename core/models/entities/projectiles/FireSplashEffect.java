package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class FireSplashEffect implements ProjectileEffect {
    private final double SPLASH_RADIUS = 1.5;


    @Override
    public void applyEffect(Zombie zombie, GameSession gameSession, Projectile projectile) {
        int splashDamage = projectile.getDamage() / 2;

        List<Zombie> nearbyZombies = gameSession.getArena().getZombiesInRadius(
                zombie.getCol(), zombie.getRow(), SPLASH_RADIUS
        );

        for (Zombie z : nearbyZombies) {
            if (z.isDead()) continue;

            z.removeChillEffect();
            z.removeFreezeEffect();

            if (z != zombie) {
                z.takeDamage(splashDamage);
            }
        }

        System.out.println("🔥 Fire Splash applied! Melted ice on nearby zombies.");
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
        return true;
    }
}
