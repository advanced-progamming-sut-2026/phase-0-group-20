package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.game.GameSession;

public class MagnetStrategy implements IPlantStrategy {
    private final int COOLDOWN_TICKS = 10 * 10;
    private int lastStealTick = 0;

    @Override
    public void execute(Plant context, int currentTick , GameSession session) {
        if (currentTick - lastStealTick >= COOLDOWN_TICKS) {
            // Logic to scan for metallic armor in an area (e.g., 5x5 grid)
            boolean foundMetal = false; // Replace with actual check

            if (foundMetal) {
                System.out.println(context.getName() + " stole a metallic object!");
                // Code to remove the armor from the target zombie

                lastStealTick = currentTick;
            }
        }
    }
}
