package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.enums.plants.ProjectileType;

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
        int row = plant.getPlacedTile().getRow();
        int col = plant.getPlacedTile().getCol();

        int damage = 1000; // damage ziad yani cheghad?

        ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());

        if (type != null) {
            Projectile.spawnNewProjectile(
                    plant,
                    type,
                    damage,
                    new Position(col, row),
                    1,
                    0,
                    true, //clean whole line
                    false
            );
        }

        notify(plant.getName() + " unleashed a lane-clearing attack: " + description);
    }
}
