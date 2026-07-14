package models.entities.plants.PlantFoodStrategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.projectiles.ProjectileMechanism;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

/**
 * Unleashes one powerful, lane-clearing attack that pierces/damages every
 * zombie in the lane.
 * Used by: Citron (plasma ball that clears the whole lane), Cactus (a burst
 * of high-damage, infinite-pierce electrified spikes).
 */

public class LaneClearFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public LaneClearFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void executeStrategy(Plant plant) {
        GameSession gameSession = GameSession.getInstance();
        int row = plant.getPlacedTile().getRow();
        int col = plant.getPlacedTile().getCol();

        int damage = 1000; // damage ziad yani cheghad?

        ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());

        if (type != null) {
            Projectile.spawnNewProjectile(
                    plant,
                    type,
                    gameSession,
                    damage,
                    new Position(col, row),
                    1,
                    0,
                    true, //clean whole line
                    false
            );
        }

        System.out.println(plant.getName() + " unleashed a lane-clearing attack: " + description);
    }
}
