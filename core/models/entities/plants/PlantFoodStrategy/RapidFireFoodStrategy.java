package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

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

    public RapidFireFoodStrategy() {
        this(0);
    }

    public RapidFireFoodStrategy(int extraGiantShots) {
        this.extraGiantShots = extraGiantShots;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " unleashed a rapid-fire barrage down its lane!");
        if (extraGiantShots > 0) {
            System.out.println(plant.getName() + " also fired " + extraGiantShots + " giant projectile(s) (20x damage)!");
        }
    }
}
