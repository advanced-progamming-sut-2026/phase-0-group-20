package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Mint Buff Strategy:
 * Applies a temporary Plant Food effect (or specific stat boost) to all plants
 * belonging to the same family as this mint across the entire board.
 */

public class MintBuffStrategy implements IPlantStrategy {
    private boolean isActivated = false;
    private int aliveTicks = 0;

    private final int lifespanInTicks = 10 * TimeManager.TICKS_PER_SECOND;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (!isActivated) {
            String mintName = context.getName().toLowerCase();

            for (Plant p : gameSession.getArena().getActivePlants()) {
                if (isSameFamily(mintName, p)) {
                    p.useFood();
                }
            }

//            gameSession.resetCooldownForTag(PlantTag.PEA);

            isActivated = true;
            System.out.println("🌿 " + context.getName() + " activated its family buff and reset cooldowns!");
        }

        aliveTicks++;
        if (aliveTicks >= lifespanInTicks) {
            context.takeDamage(context.getCurrentHp());
        }
    }

    private boolean isSameFamily(String mintName, Plant targetPlant) {
        return switch (mintName) {
            case "enlighten-mint" -> targetPlant.getCategory() == PlantCategory.SUN_PRODUCER;
            case "appease-mint" -> targetPlant.getCategory() == PlantCategory.SHOOTER;
            case "arma-mint" -> targetPlant.getCategory() == PlantCategory.LOBBER;
            case "bombard-mint" -> targetPlant.getCategory() == PlantCategory.EXPLOSIVE;
            case "enforce-mint" -> targetPlant.getCategory() == PlantCategory.MELEE;
            case "reinforce-mint" -> targetPlant.getCategory() == PlantCategory.WALL_NUT;
            case "enchant-mint" -> targetPlant.getCategory() == PlantCategory.MODIFIER;
            case "pierce-mint" -> targetPlant.getCategory() == PlantCategory.STRIKE_THROUGH;
            case "cattail-mint" -> targetPlant.getCategory() == PlantCategory.HOMING;
            default -> false;
        };
    }
}
