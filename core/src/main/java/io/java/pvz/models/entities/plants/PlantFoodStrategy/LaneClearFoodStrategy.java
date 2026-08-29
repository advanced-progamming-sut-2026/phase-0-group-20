// file: core/src/main/java/io/java/pvz/models/entities/plants/PlantFoodStrategy/LaneClearFoodStrategy.java
package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class LaneClearFoodStrategy implements PlantFoodStrategy {

    private final String description;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public LaneClearFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        float animLength = (anim != null && anim.hasClip("plantfood")) ?
            anim.getDuration("plantfood") : 2.5f;

        this.durationTicks = this.setupTicks + (int) (animLength * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        int fireTick = setupTicks;
        if (plant.getName().equalsIgnoreCase("Citron")) {
            fireTick = Math.max(setupTicks, durationTicks - (int)(0.5f * TimeManager.TICKS_PER_SECOND));
        }

        if (!executed && tickTimer > fireTick) {
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            int damage = 1800;

            ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());

            if (type != null) {
                Projectile projectile = Projectile.spawnNewProjectile(
                    plant,
                    type,
                    damage,
                    new Position(col, row),
                    ProjectileTuning.speedFor(type),
                    0,
                    true,
                    false
                );
                projectile.setSpawnDelayTicks(0.1f);
            }
            executed = true;
        }
    }

    @Override
    public void onExit(Plant plant) {
        if (plant.getName().equalsIgnoreCase("Citron")) {
            plant.triggerAction("recovery");
        } else {
            PlantFoodStrategy.super.onExit(plant);
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.executed = false;
        this.tickTimer = 0;
    }
}
