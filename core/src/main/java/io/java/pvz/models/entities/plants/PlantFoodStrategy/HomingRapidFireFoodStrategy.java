package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;


public class HomingRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 6 * TimeManager.TICKS_PER_SECOND;
    private int tickTimer = 0;


    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession session = GameSession.getInstance();

        if (tickTimer <= durationTicks) {
            if (tickTimer % (TimeManager.TICKS_PER_SECOND / 5) == 0) {
                int col = plant.getPlacedTile().getCol();
                int row = plant.getPlacedTile().getRow();

                Zombie target = session.getArena().getNearestZombie(col, row); //we should implement it later

                if (target != null && !target.isDead())
                    ProjectileMechanism.executeTargetedProjectile(plant, target, 0.1f);
                else
                    ProjectileMechanism.executeNewProjectile(plant, true, false, 0.1f);
            }
        }
    }


    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.tickTimer = 0;
    }
}
