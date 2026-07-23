package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.SnorkelContext;
import models.enums.plants.ProjectileType;

public class SnorkelDefense implements DefenseBehavior {
    private final Zombie zombie;
    private final SnorkelContext context;

    public SnorkelDefense(Zombie zombie, SnorkelContext context) {
        this.zombie = zombie;
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
