package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Hypnotize Strategy:
 * When eaten by a zombie, this strategy reverses the zombie's direction
 * and makes it fight for the player instead of eating plants.
 */

public class HypnotizeStrategy implements IPlantStrategy {

    @Override
    public void execute(Plant context, int currentTick) {
        // Typically, this effect is triggered when the plant's HP drops or it gets eaten.
        if (context.getCurrentHp() <= 0) {
            System.out.println(context.getName() + " was eaten! Hypnotizing the attacker...");
            // Logic to find the zombie that dealt the fatal blow and change its faction/direction
        }
    }
}
