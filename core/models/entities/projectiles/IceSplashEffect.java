package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.game.GameSession;

import java.util.List;

public class IceSplashEffect implements ProjectileEffect {
    private static final double SPLASH_RADIUS = 1.5;
    private static final int CHILL_DURATION_TICKS = 30;
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

            zombie.addEffect(new ChillEffect(zombie, CHILL_DURATION_TICKS)); //fully stop

            if (z != zombie) {
                z.takeDamage(splashDamage);
                if (z.isDead()) {
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
