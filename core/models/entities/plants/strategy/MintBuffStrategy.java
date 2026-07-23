package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.enums.plants.PlantCategory;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Mint Buff Strategy:
 * Applies a temporary Plant Food effect (or specific stat boost) to all plants
 * belonging to the same family as this mint across the entire board.
 */

public class MintBuffStrategy implements IPlantStrategy {
    private int lifespanInTicks = 10 * TimeManager.TICKS_PER_SECOND;
    private boolean isActivated = false;
    private int aliveTicks = 0;

    private boolean resetCooldowns = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (!isActivated) {
            String mintName = context.getName().toLowerCase();
            PlantCategory familyCategory = getMintCategory(mintName);

            for (Plant p : GameSession.getInstance().getArena().getActivePlants()) {
                if (isSameFamily(mintName, p)) {
                    p.useFood();
                }
            }

            if (resetCooldowns && familyCategory != null) {
                GameSession.getInstance().resetCooldownsForCategory(familyCategory);
                notify("⏳ " + context.getName() +
                        " instantly refreshed all " + familyCategory.name() + " seed packets!");
            }
            isActivated = true;
            notify("🌿 " + context.getName() + " activated its family buff and reset cooldowns!");
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

    private PlantCategory getMintCategory(String mintName) {
        return switch (mintName) {
            case "enlighten-mint" -> PlantCategory.SUN_PRODUCER;
            case "appease-mint" -> PlantCategory.SHOOTER;
            case "arma-mint" -> PlantCategory.LOBBER;
            case "bombard-mint" -> PlantCategory.EXPLOSIVE;
            case "enforce-mint" -> PlantCategory.MELEE;
            case "reinforce-mint" -> PlantCategory.WALL_NUT;
            case "enchant-mint" -> PlantCategory.MODIFIER;
            case "pierce-mint" -> PlantCategory.STRIKE_THROUGH;
            case "cattail-mint" -> PlantCategory.HOMING;
            default -> null;
        };
    }

    public void increaseBoostDuration(float extraSeconds) {
        this.lifespanInTicks += (int) (extraSeconds * TimeManager.TICKS_PER_SECOND);
    }

    public void setResetCooldowns(boolean resetCooldowns) {
        this.resetCooldowns = resetCooldowns;
    }
}
