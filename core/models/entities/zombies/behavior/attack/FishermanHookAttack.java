package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.tiles.Tile;
import models.game.GameSession;

public class FishermanHookAttack implements AttackBehavior {

    private final Zombie zombie;

    public FishermanHookAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        int row = zombie.getRow();

        Plant target = session.getArena().nearestPlantInRow(zombie, row);

        if (target != null) {
            int currentCol = target.getPlacedTile().getCol();
            int newCol = currentCol + 1;

            if (newCol < session.getArena().getCols()) {
                Tile newTile = session.getArena().getTile(row, newCol);

                if (newTile.isPlantable(target)) {
                    target.getPlacedTile().getPlants().remove(target);
                    target.setPlacedTile(newTile);
                    newTile.addPlant(target);
                }
            }
        }
    }
}
