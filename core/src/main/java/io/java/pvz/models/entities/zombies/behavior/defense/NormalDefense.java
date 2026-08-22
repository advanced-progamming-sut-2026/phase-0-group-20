package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.projectiles.ProjectileType;

public class NormalDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
