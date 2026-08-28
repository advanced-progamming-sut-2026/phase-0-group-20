// file: core/src/main/java/io/java/pvz/models/entities/plants/PlantFoodStrategy/RapidFireFoodStrategy.java
package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.timeManager.TimeManager;

public class RapidFireFoodStrategy implements PlantFoodStrategy {

    private final int minDuration = 6 * TimeManager.TICKS_PER_SECOND;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private final int extraGiantShots;
    private final boolean doesRapidFire;
    private int tickTimer = 0;
    private int giantShotsFired = 0;
    private int totalGiantShots = 0;

    public RapidFireFoodStrategy() {
        this(0, true);
    }

    public RapidFireFoodStrategy(int extraGiantShots, boolean doesRapidFire) {
        this.extraGiantShots = extraGiantShots;
        this.doesRapidFire = doesRapidFire;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.tickTimer = 0;
        this.giantShotsFired = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];

        if (plant.getName().equalsIgnoreCase("Pea Pod")) {
            this.totalGiantShots = plant.getStackCount();

            this.durationTicks = this.setupTicks + (this.totalGiantShots * (TimeManager.TICKS_PER_SECOND));
        } else {
            this.totalGiantShots = extraGiantShots;
            this.durationTicks = Math.max(minDuration + setupTicks, timings[1]);
        }
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer > setupTicks) {
            int activeTick = tickTimer - setupTicks;

            if (doesRapidFire && tickTimer <= durationTicks) {
                if (activeTick % (TimeManager.TICKS_PER_SECOND / 5) == 0)
                    ProjectileMechanism.executeNewProjectile(plant, true, false, 0.1f);
            }

            if (giantShotsFired < totalGiantShots) {
                if (activeTick % (TimeManager.TICKS_PER_SECOND / 2) == 0) {
                    if (plant.getName().equalsIgnoreCase("Pea Pod") &&
                        (activeTick + TimeManager.TICKS_PER_SECOND / 2) % (TimeManager.TICKS_PER_SECOND) != 0)
                        return;
                    ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());
                    int giantDamage = plant.getDamage() * 20;
                    int col = plant.getPlacedTile().getCol();
                    int row = plant.getPlacedTile().getRow();

                    Projectile projectile = Projectile.spawnNewProjectile(
                        plant,
                        type,
                        giantDamage,
                        new Position(col, row),
                        ProjectileTuning.speedFor(type),
                        0,
                        false,
                        false
                    );

                    projectile.setSpawnDelayTicks(0.1f);
                    projectile.setSize(2);

                    giantShotsFired++;
                }
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
        this.giantShotsFired = 0;
    }
}
