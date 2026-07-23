package models.entities.zombies.behavior.defense;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.JugglerContext;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

public class JugglerDefense implements DefenseBehavior {
    private final Zombie zombie;
    private final JugglerContext context;

    public JugglerDefense(Zombie zombie, JugglerContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (deflectProjectile(damageType)) {
            return 0;
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        if (ProjectileType.isDeflectByJuggler(projectileType)) {
            context.triggerSpin();
            GameSession.notify("Juggler started spinning!");

            reflectToPlant(projectileType);

            return true;
        }
        return false;
    }

    private void reflectToPlant(ProjectileType type) {
        GameSession session = GameSession.getInstance();
        Plant targetPlant = null;
        int maxCol = -1;

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile().getRow() == zombie.getRow() && p.getPlacedTile().getCol() <= zombie.getCol()) {
                if (p.getPlacedTile().getCol() > maxCol) {
                    maxCol = p.getPlacedTile().getCol();
                    targetPlant = p;
                }
            }
        }

        if (targetPlant != null) {
            if (type == ProjectileType.ICE_PEA) {
                targetPlant.receiveIceHit();
                GameSession.notify("Juggler reflected ICE to " + targetPlant.getName() + "!");
            } else {
                targetPlant.takeDamage(20);
                GameSession.notify("Juggler reflected projectile to " + targetPlant.getName() + "!");
            }
        }
    }
}
