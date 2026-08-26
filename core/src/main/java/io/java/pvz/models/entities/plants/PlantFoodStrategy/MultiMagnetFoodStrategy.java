package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class MultiMagnetFoodStrategy implements PlantFoodStrategy {

    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];

        this.durationTicks = Math.max(1, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
            GameSession gameSession = GameSession.getInstance();
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            double range = 15.0;

            List<Zombie> targets = gameSession.getArena().getZombiesInRadius(col, row, range);

            for (Zombie zombie : targets) {
                if (zombie.isDead() || zombie.getRow() != row) continue;

                for (Armor armor : zombie.getArmorPieces()) {
                    if (!armor.isDestroyed() && armor.isMetallic())
                        armor.takeDamage(99999);

                    if (!zombie.getArmorPieces().isEmpty())
                        zombie.getArmorPieces().clear();
                }
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
        this.tickTimer = 0;
    }
}
