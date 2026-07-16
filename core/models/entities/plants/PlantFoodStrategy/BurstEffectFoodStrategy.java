package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;

import java.util.List;

/**
 * A single powerful area-effect burst centered on (or just in front of) the
 * plant - melee smashes, smoke blasts, slams, etc.
 * Used by: Fume-shroom (giant smoke cloud that pushes zombies back),
 * Bonk Choy (rapid 3x3 punches), Phat Beet (powerful sonic 3x3 blast),
 * Wasabi Whip (spinning whip in 3x3), Kiwibeast (jump + ground slam AoE).
 */

public class BurstEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;

    public BurstEffectFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void executeStrategy(Plant plant) {

        GameSession gameSession = GameSession.getInstance();
        String name = plant.getName().toLowerCase();
        int row = plant.getPlacedTile().getRow();
        int col = plant.getPlacedTile().getCol();
        int damage = 1500; //damage ziad

        if (name.equalsIgnoreCase("fume-shroom")) {
            for (Zombie zombie : gameSession.getArena().zombieInRow(row)) {
                if (!zombie.isDead() && zombie.getCol() >= col) {
                    boolean killed = zombie.takeDirectDamage(damage);
                    if(killed){
                        plant.onZombieDeath(zombie);
                    }

                    float pushBackDistance = PhysicalConstants.TILE_UNIT_LENGTH * 3;
                    zombie.getPosition().moveX(pushBackDistance);

                    if (zombie.getCol() >= gameSession.getArena().getCols())
                        zombie.getPosition().setCol(gameSession.getArena().getCols() - 1);
                }
            }
        } else {
            List<Zombie> nearZombies = gameSession.getArena().getZombiesInRadius(col, row, 1.5);
            for (Zombie zombie : nearZombies) {
                if (zombie.isDead()) continue;
                boolean killed =zombie.takeDirectDamage(damage);
                if(killed){
                   plant.onZombieDeath(zombie);
                }
            }
        }

        notify(plant.getName() + " unleashed an area burst: " + description);
    }
}