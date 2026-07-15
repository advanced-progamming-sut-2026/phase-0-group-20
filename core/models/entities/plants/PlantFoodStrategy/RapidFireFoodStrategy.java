package models.entities.plants.PlantFoodStrategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.projectiles.ProjectileMechanism;
import models.enums.plants.ProjectileType;


/**
 * Fires a fast barrage of shots down this plant's lane for a few seconds.
 * Used by: Peashooter, Repeater (+1 giant pea), Threepeater's single-lane
 * cousins, Goo Peashooter (poison), Fire Peashooter (fire), Mega Gatling Pea
 * (+4 giant peas), Pea Pod (+1 giant pea per stacked head), Cat-tail (homing).
 * `extraGiantShots` represents the bonus giant/empowered projectiles some
 * plants also launch alongside the barrage (0 = none).
 */

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
                ProjectileMechanism.executeNewProjectile(plant, true, false);
            if (tickTimer == 2)
                System.out.println(plant.getName() + " unleashed a rapid-fire barrage!");
            return;
        }

        if (giantShotsFired < totalGiantShots) {  //shoot giant pea if needed

            if (tickTimer % 5 == 0) { //giant shots take more time
                ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());
                int giantDamage = ProjectileMechanism.parseDamage(plant.getDamage()) * 20;
                int col = plant.getPlacedTile().getCol();
                int row = plant.getPlacedTile().getRow();

                Projectile projectile = Projectile.spawnNewProjectile(
                        plant,
                        type,
                        giantDamage,
                        new Position(col, row),
                        1,
                        0,
                        false,
                        false
                );
                projectile.setSize(2);

                giantShotsFired++;

                System.out.println(plant.getName() + " fired giant projectile " + giantShotsFired + "/" + totalGiantShots);
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
