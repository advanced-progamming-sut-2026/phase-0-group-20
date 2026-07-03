package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Mint Buff Strategy:
 * Applies a temporary Plant Food effect (or specific stat boost) to all plants
 * belonging to the same family as this mint across the entire board.
 */

public class MintBuffStrategy implements IPlantStrategy {
    private boolean buffApplied = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (!buffApplied) {
            System.out.println(context.getName() + " applied a global buff to its entire plant family!");

            // Logic to iterate over all plants on the board and apply buff if they match the family
            buffApplied = true;
        }
    }
}
