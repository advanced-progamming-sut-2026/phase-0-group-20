package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class MultiLaneRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int minDuration = 6 * TimeManager.TICKS_PER_SECOND;
    private int durationTicks = 0;
    private int setupTicks = 0;

    private int currentRow = 0;
    private int directionCoeff = 1;
    private int tickTimer = 0;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.tickTimer = 0;
        this.currentRow = 0;
        this.directionCoeff = 1;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
        this.durationTicks = Math.max(minDuration + setupTicks, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer > setupTicks && tickTimer <= durationTicks) {
            int activeTick = tickTimer - setupTicks;

            if (activeTick % (TimeManager.TICKS_PER_SECOND / 5) == 0) {
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
        }
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
