package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.enums.plants.ProjectileType;

public interface DefenseBehavior {
    int mitigateDamage(int damage, ProjectileType damageType); // return damage after defense

    boolean deflectProjectile(ProjectileType projectileType);
}
