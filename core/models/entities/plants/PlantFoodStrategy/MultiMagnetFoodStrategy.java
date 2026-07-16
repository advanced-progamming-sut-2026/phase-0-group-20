package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.armour.Armor;
import models.game.GameSession;

import java.util.List;

/**
 * Instantly pulls metallic items off of every zombie in range at once,
 * instead of the normal one-at-a-time, on-cooldown behavior.
 * Used by: Magnet-shroom.
 */

public class MultiMagnetFoodStrategy implements PlantFoodStrategy {

    @Override
    public void executeStrategy(Plant plant) {
        GameSession gameSession = GameSession.getInstance();
        int row = plant.getPlacedTile().getRow();
        int col = plant.getPlacedTile().getCol();
        double range = 15.0;

        List<Zombie> targets = gameSession.getArena().getZombiesInRadius(col, row, range);

        for (Zombie zombie : targets) {
            if (zombie.isDead() || zombie.getRow() != row) continue;

            for (Armor armor : zombie.getArmorPieces()) {
                if (!armor.isDestroyed() && armor.isMetallic()) {
                    armor.takeDamage(99999);
                    notify(plant.getName() + " magnetically yanked " + armor.getData().getAlias() + " off " + zombie.getName() + "!");
                }
                if (!zombie.getArmorPieces().isEmpty())
                    zombie.getArmorPieces().clear();

            }
            notify(plant.getName() + " yanked every metallic item in range off zombies at once!");
        }
    }
}
