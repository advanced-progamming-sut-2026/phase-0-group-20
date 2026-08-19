package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.game.GameSession;

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
        int plantCol = plant.getPlacedTile().getCol();

        Projectile projectile = Projectile.spawnNewProjectile(
            plant,
            type,
            damage,
            new Position(plantCol, currentRow),
            ProjectileTuning.speedFor(type),
            0,
            false,
            false);

        projectile.setSpawnDelayTicks(0.1f);
        if (currentRow >= session.getArena().getRows() - 1) directionCoeff = -1;
        else if (currentRow <= 0) directionCoeff = 1;

        currentRow += directionCoeff;

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
