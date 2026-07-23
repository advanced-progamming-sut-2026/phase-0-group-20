package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class FireSplashEffect implements ProjectileEffect {
    private double splashRadius;

    public FireSplashEffect(double splashRadius) {
        this.splashRadius = splashRadius;
    }

    @Override
    public void applyEffect(Zombie zombie, Projectile projectile) {
        int splashDamage = projectile.getDamage() / 2;


        List<Zombie> nearbyZombies = GameSession.getInstance().getArena().getZombiesInRadius(
                zombie.getCol(), zombie.getRow(), splashRadius
        );

        for (Zombie z : nearbyZombies) {
            if (z.isDead()) continue;

            z.removeChillEffect();
            z.removeFreezeEffect();

            if (z != zombie) {
                z.takeDamage(splashDamage);
                if (z.isDead()) {
                    projectile.getPlant().onZombieDeath(z);
                }
            }
        }

        notify("🔥 Fire Splash applied! Melted ice on nearby zombies.");
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
