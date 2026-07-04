package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Digestion Strategy:
 * Used for plants like Chomper. The plant instantly kills (swallows) a zombie in range,
 * then enters a long "digestion" state where it becomes vulnerable and inactive.
 */

public class DigestionStrategy implements IPlantStrategy {
    private final int DIGESTION_DURATION_TICKS = 40 * 10;
    private boolean isDigesting = false;
    private int digestionStartTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (isDigesting) {
            // Check if digestion is complete
            if (currentTick - digestionStartTick >= DIGESTION_DURATION_TICKS) {
                isDigesting = false;
                System.out.println(context.getName() + " finished digesting and is ready to attack again!");
            }
        } else {
            // Logic to check for a zombie in the adjacent tile
            boolean zombieInRange = false; // Replace with actual collision check

            if (zombieInRange) {
                System.out.println(context.getName() + " swallowed a zombie!");
                isDigesting = true;
                digestionStartTick = currentTick;
                // Code to instantly kill the zombie goes here
            }
        }
    }
}
