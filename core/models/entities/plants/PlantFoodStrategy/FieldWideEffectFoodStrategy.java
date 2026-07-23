package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.entities.zombies.behavior.effect.FreezeEffect;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.game.adventure.SeasonType;

import java.util.List;
import java.util.Random;

/**
 * A single effect applied to every zombie on the field (or every zombie
 * currently visible on screen).
 * Used by: Garlic (force every zombie in the lane to move to another lane),
 * Kernel-pult (drop butter on every zombie's head, stunning them), Iceberg
 * Lettuce (freeze every zombie currently visible), Sweet Potato (pull in
 * every nearby zombie and fully heal itself).
 */

public class FieldWideEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public FieldWideEffectFoodStrategy(String description) {
        this.description = description;
    }


    @Override
    public void executeStrategy(Plant plant) {
        GameSession gameSession = GameSession.getInstance();
        List<Zombie> allZombies = gameSession.getArena().getActiveZombies();
        String plantName = plant.getName().toLowerCase();

        int plantRow = plant.getPlacedTile().getRow();
        int plantCol = plant.getPlacedTile().getCol();

        switch (plantName) {
            case "iceberg lettuce":
                for (Zombie zombie : allZombies)
                    if (!zombie.isDead())
                        if (gameSession.getCurrentChapter().getSeasonType() == SeasonType.FROZEN_CAVES)
                            zombie.addEffect(new ChillEffect(zombie, 150));
                        else
                            zombie.addEffect(new FreezeEffect(zombie, 150));
                break;

            case "kernel-pult":
                for (Zombie zombie : allZombies)
                    if (!zombie.isDead())
                        ProjectileMechanism.executeTargetedProjectile(plant, zombie, 0);
                break;

            case "garlic":
                for (Zombie zombie : gameSession.getArena().zombieInRow(plantRow))
                    if (!zombie.isDead())
                        shiftZombieToAdjacentLane(zombie, gameSession);
                break;

            case "sweet potato":
                plant.setCurrentHp(plant.getBaseHp());
                List<Zombie> nearby = gameSession.getArena().getZombiesInRadius(
                        plantCol, plantRow, PhysicalConstants.TILE_UNIT_LENGTH * 2);
                for (Zombie zombie : nearby)
                    if (!zombie.isDead() && zombie.getRow() != plantRow)
                        zombie.setRow(plantRow);
                break;

            default:
                notify("WARNING: Unmapped field-wide effect for: " + plant.getName());
        }

        notify(plant.getName() + " triggered a field-wide effect: " + description);
    }

    private void shiftZombieToAdjacentLane(Zombie zombie, GameSession gameSession) {
        int currentRow = zombie.getRow();
        int maxRows = gameSession.getArena().getRows();

        boolean canGoUp = (currentRow > 0);
        boolean canGoDown = (currentRow < maxRows - 1);

        if (canGoUp && canGoDown)
            zombie.setRow(currentRow - 1 + new Random().nextInt(2) * 2); //current row - 1 + (0 or 2)
        else if (canGoUp)
            zombie.setRow(currentRow - 1);
        else if (canGoDown)
            zombie.setRow(currentRow + 1);
        else
            zombie.setState(ZombieState.STUNNED);
    }
}
