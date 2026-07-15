package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.entities.zombies.Zombie;
import models.game.GameSession;


public class HomingRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 60;
    private int tickTimer = 0;


    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession session = GameSession.getInstance();

        if (tickTimer <= durationTicks) {
            if (tickTimer % 2 == 0) {
                int col = plant.getPlacedTile().getCol();
                int row = plant.getPlacedTile().getRow();

                Zombie target = session.getArena().getNearestZombie(col, row); //we should implement it later

                if (target != null && !target.isDead())
                    ProjectileMechanism.executeTargetedProjectile(plant, target, 0);
                else
                    ProjectileMechanism.executeNewProjectile(plant, true, false);

            }

            if (tickTimer == 2)
                System.out.println(plant.getName() + " unleashed a targeted rapid-fire barrage!");

        }
    }


    public int getDurationTicks() {
        return durationTicks;
    }
}