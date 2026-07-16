package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class AreaOfEffect implements ProjectileEffect {
    private ProjectileEffect inner;
    private int splashDamage;
    private double radius;

    public AreaOfEffect(ProjectileEffect inner, int splashDamage, double radius) {
        this.inner = inner;
        this.splashDamage = splashDamage;
        this.radius = radius;
    }

    @Override
    public void applyEffect(Zombie mainZombie, Projectile projectile) {
        if (inner != null) inner.applyEffect(mainZombie, projectile);

        List<Zombie> nearby = GameSession.getInstance().getArena().getZombiesInRadius((int) projectile.getY(), (int) projectile.getY(), radius);
        for (Zombie zombie : nearby) {
            if (zombie == mainZombie) continue;
            boolean killed =zombie.takeDamage(splashDamage, projectile);
            if(killed){
                projectile.getPlant().onZombieDeath(zombie);
            }
            if (inner != null) inner.applyEffect(zombie, projectile);
        }
    }

    @Override
    public int getDamageMultiplier() {
        return inner != null ? inner.getDamageMultiplier() : 1;
    }

    @Override
    public boolean ignoresArmor() {
        return inner != null && inner.ignoresArmor();
    }

    @Override
    public boolean meltsIce() {
        return inner != null && inner.meltsIce();
    }
}
