package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

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
    public void applyEffect(Zombie mainZombie, Arena board, Projectile projectile) {
        if (inner != null) inner.applyEffect(mainZombie, board, projectile);

        List<Zombie> nearby = board.getZombiesInRadius(projectile.getColumn(), projectile.getLane(), radius);
        for (Zombie zombie : nearby) {
            if (zombie == mainZombie) continue;
            zombie.takeDamage(splashDamage);
            if (inner != null) inner.applyEffect(zombie, board, projectile);
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
