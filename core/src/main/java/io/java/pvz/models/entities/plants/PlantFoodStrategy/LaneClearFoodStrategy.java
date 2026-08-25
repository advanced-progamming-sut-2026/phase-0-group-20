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
    private boolean executed = false;

    public LaneClearFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;

        float animDuration = 1.0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) animDuration = anim.getDuration("plantfood_on") +
                anim.getDuration("plantfood");
            else if (anim.hasClip("plantfood")) animDuration = anim.getDuration("plantfood");
        }
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            int damage = 1800; // damage ziad yani cheghad?

            ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());

            if (type != null) {
                Projectile projectile = Projectile.spawnNewProjectile(
                    plant,
                    type,
                    damage,
                    new Position(col, row),
                    ProjectileTuning.speedFor(type),
                    0,
                    true, //clean whole line
                    false
                );
                projectile.setSpawnDelayTicks(0.5f);
            }
            executed = true;
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.executed = false;
    }
}
