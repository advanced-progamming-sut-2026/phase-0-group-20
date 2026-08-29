package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.JugglerContext;
import io.java.pvz.models.entities.zombies.behavior.effect.JugglerDeflectEffect;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

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
            GameEventMessenger.getInstance().dispatch(GameEvent.EFFECTS,
                new GameEventPayload.Builder(GameEvent.EFFECTS)
                    .message("DEFLECT_PROJECTILE")
                    .zombie(zombie)
                    .plant(targetPlant)
                    .projectileType(type)
                    .build());

            zombie.addEffect(new JugglerDeflectEffect(zombie, targetPlant, type));
        }
    }
}
