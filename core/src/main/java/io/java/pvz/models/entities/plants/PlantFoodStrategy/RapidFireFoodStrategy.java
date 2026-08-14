package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.enums.plants.ProjectileType;


public class RapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 60;
    private final int extraGiantShots;
    private final boolean doesRapidFire;
    private int tickTimer = 0;
    private int giantShotsFired = 0;
    private int totalGiantShots = -1;

    public RapidFireFoodStrategy() {
        this(0, true);
    }

    public RapidFireFoodStrategy(int extraGiantShots, boolean doesRapidFire) {
        this.extraGiantShots = extraGiantShots;
        this.doesRapidFire = doesRapidFire;
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (totalGiantShots == -1) {
            if (plant.getName().equalsIgnoreCase("Pea Pod"))
                totalGiantShots = plant.getStackCount(); //each head
            else
                totalGiantShots = extraGiantShots;

        }


        if (doesRapidFire && tickTimer <= durationTicks) {
            if (tickTimer % 2 == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, false, 0.1f);
            if (tickTimer == 2)
                notify(plant.getName() + " unleashed a rapid-fire barrage!");
        }

        if (giantShotsFired < totalGiantShots) {  //shoot giant pea if needed

            if (tickTimer % 5 == 0) { //giant shots take more time
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

                notify(plant.getName() + " fired giant projectile " + giantShotsFired + "/" + totalGiantShots);
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
        this.totalGiantShots = -1;
    }
}
