package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

/**
 * Charge Strategy:
 * The plant requires a long uninterrupted period to charge its attack.
 * Once fully charged, it holds the attack until a target appears.
 */

public class ChargeStrategy implements IPlantStrategy {
    private int chargeStartTick = -1;
    private boolean isFullyCharged = false;

    @Override
    public void execute(Plant context, int currentTick) {
        int requiredChargeTicks = (int) (context.getActionInterval() * 10);

        if (!isFullyCharged) {
            if (chargeStartTick == -1) chargeStartTick = currentTick;

            if (currentTick - chargeStartTick >= requiredChargeTicks) {
                isFullyCharged = true;
                System.out.println(context.getName() + " is fully charged!");
            }
        } else {
            // If target is present, shoot and reset charge
            boolean targetPresent = true; // Replace with actual check
            if (targetPresent) {
                System.out.println(context.getName() + " fired its charged attack!");
                isFullyCharged = false;
                chargeStartTick = currentTick; // Start charging again
            }
        }
    }
}
