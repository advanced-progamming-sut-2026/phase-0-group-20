package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.move.SnorkelMove;
import models.enums.plants.ProjectileType;

public class SnorkelDefense implements DefenseBehavior{
    private final Zombie zombie;

    public SnorkelDefense(Zombie zombie) {
        this.zombie = zombie;
    }


    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (isSubmerged() && !isLobbedShot(damageType)) {
            return 0;
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }

    private boolean isSubmerged() {
        return zombie.getMoveBehavior() instanceof SnorkelMove snorkelMove && snorkelMove.isSubmerged();
    }

    private boolean isLobbedShot(ProjectileType projectileType) {
        if (projectileType == null) return false;
        return switch (projectileType) {
            case CABBAGE, CORN, BUTTER, MELON, WINTER_MELON, PEPPER -> true;
            default -> false;
        };
    }
}
