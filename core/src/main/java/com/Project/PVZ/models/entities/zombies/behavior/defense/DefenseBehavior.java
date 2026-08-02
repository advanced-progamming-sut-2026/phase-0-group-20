package com.Project.PVZ.models.entities.zombies.behavior.defense;

import com.Project.PVZ.models.enums.plants.ProjectileType;

public interface DefenseBehavior {
    int mitigateDamage(int damage, ProjectileType damageType); // return damage after defense

    boolean deflectProjectile(ProjectileType projectileType);
}
