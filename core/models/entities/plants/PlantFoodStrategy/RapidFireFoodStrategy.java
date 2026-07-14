package models.entities.plants.PlantFoodStrategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.projectiles.ProjectileMechanism;
import models.enums.plants.ProjectileType;
import models.game.GameSession;


/**
 * Fires a fast barrage of shots down this plant's lane for a few seconds.
 * Used by: Peashooter, Repeater (+1 giant pea), Threepeater's single-lane
 * cousins, Goo Peashooter (poison), Fire Peashooter (fire), Mega Gatling Pea
 * (+4 giant peas), Pea Pod (+1 giant pea per stacked head), Cat-tail (homing).
 * `extraGiantShots` represents the bonus giant/empowered projectiles some
 * plants also launch alongside the barrage (0 = none).
 */

public class RapidFireFoodStrategy implements PlantFoodStrategy {

    private final int extraGiantShots;
    private int tickTimer;

    public RapidFireFoodStrategy() {
        this(0);
    }

    public RapidFireFoodStrategy(int extraGiantShots) {
        this.extraGiantShots = extraGiantShots;
        this.tickTimer = 0;
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer % 2 == 0)
            ProjectileMechanism.executeNewProjectile(plant, true, false);

        if (tickTimer == 2)
            System.out.println(plant.getName() + " unleashed a rapid-fire barrage down its lane!");

        if (extraGiantShots > 0 && plant.getBoostTimer() - tickTimer < 2) { // shoot giant pea as last shot
            ProjectileType type = ProjectileMechanism.getProjectileType(plant.getName());
            int giantDamage = ProjectileMechanism.parseDamage(plant.getDamage()) * 20;
            Projectile projectile = Projectile.spawnNewProjectile(
                    plant,
                    type,
                    giantDamage,
                    new Position(plant.getPlacedTile().getCol() + 1, plant.getPlacedTile().getRow()),
                    1,
                    0,
                    false,
                    false
            );
            projectile.setSize(2);
            tickTimer = 0;
            System.out.println(plant.getName() + " also fired " + extraGiantShots + " giant projectile(s) (20x damage)!");
        }
    }
}
