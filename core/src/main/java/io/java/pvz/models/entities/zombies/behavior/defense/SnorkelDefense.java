package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.zombies.behavior.context.SnorkelContext;
import io.java.pvz.models.enums.plants.ProjectileType;

public class SnorkelDefense implements DefenseBehavior {
    private final SnorkelContext context;

    public SnorkelDefense( SnorkelContext context) {
        this.context = context;
    }


    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (context.isSubmerged()) {
            if (!ProjectileType.isLobbed(damageType)) {
                return 0;
            }
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }

}
