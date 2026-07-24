package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.ExplorerContext;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

public class ExplorerDefense implements DefenseBehavior {
    private final Zombie zombie;
    private final ExplorerContext context;

    public ExplorerDefense(Zombie zombie, ExplorerContext context) {
        this.zombie = zombie;
        this.context = context;
    }


    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (ProjectileType.isIceProjectile(damageType)) {
            if (context.isTorchLit()) {
                context.setTorchLit(false);
                GameSession.notify("Explorer's torch was extinguished by ice!");
            }
        } else if (ProjectileType.isFireProjectile(damageType)) {
            if (!context.isTorchLit()) {
                context.setTorchLit(true);
                System.out.println("Explorer's torch was re-ignited by fire!");
            }
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
