package models.entities.plants.PlantFoodStrategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.projectiles.ProjectileMechanism;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

/**
 * Fires a fan-shaped rapid barrage across all of this plant's lanes at once.
 * Used by: Threepeater (5 lanes bad bezan tor).
 */

public class MultiLaneRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 60;
    private int tickTimer = 0;

    private int currentRow = 0;
    private int directionCoeff = 1;

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession session = GameSession.getInstance();

        ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());
        int damage = plant.getDamage();
        float plantCol = plant.getPlacedTile().getCol();

        Projectile.spawnNewProjectile(
                plant,
                type,
                damage,
                new Position(plantCol, currentRow),
                1,
                0,
                false,
                false);

        if (currentRow >= session.getArena().getRows() - 1) directionCoeff = -1;
        else if (currentRow <= 0) directionCoeff = 1;

        currentRow += directionCoeff;


        if (tickTimer == 2)
            notify(plant.getName() + " fired a fan-shaped rapid barrage across all its lanes!");

    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.tickTimer = 0;
        this.currentRow = 0;
        this.directionCoeff = 1;
    }
}
