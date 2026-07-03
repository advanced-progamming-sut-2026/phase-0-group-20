package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Sun On Hit Strategy:
 * Used for Sun Bean. Whenever a zombie damages this plant (e.g., takes a bite),
 * it generates a specific amount of sun (e.g., 5 sun per hit).
 */

public class SunOnHitStrategy implements IPlantStrategy {
    private int lastRecordedHp = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        // Initialize the HP tracker on the first tick
        if (lastRecordedHp == -1) {
            lastRecordedHp = context.getCurrentHp();
            return;
        }

        // Check if the plant has taken damage since the last tick
        if (context.getCurrentHp() < lastRecordedHp) {
            // int damageTaken = lastRecordedHp - context.getCurrentHp();

            System.out.println(context.getName() + " was bitten! Dropped 5 sun.");
            // Logic to spawn 5 sun resources in the game world

            // Update the tracker
            lastRecordedHp = context.getCurrentHp();
        }
    }
}
