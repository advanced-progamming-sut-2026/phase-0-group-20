package com.Project.PVZ.models.entities.plants.PlantFoodStrategy;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.strategy.IPlantStrategy;
import com.Project.PVZ.models.entities.plants.strategy.LifespanStrategy;
import com.Project.PVZ.models.game.GameSession;

/**
 * Fires a rapid barrage AND resets the lifespan timer of every plant of the
 * same species currently on the board (they have a limited 60s lifespan -
 * see LifespanStrategy).
 * Used by: Sea-shroom, Puff-shroom.
 */

public class ResetLifespanFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {

        for (Plant p : GameSession.getInstance().getArena().getActivePlants())
            if (p.getName().equals(plant.getName()))
                for (IPlantStrategy strategy : p.getStrategies())
                    if (strategy instanceof LifespanStrategy lifespanStrategy)
                        lifespanStrategy.resetLifespan();

        notify(plant.getName() + "The lifespan of all "
                + plant.getName() + "s on the board has been reset!");
    }
}
