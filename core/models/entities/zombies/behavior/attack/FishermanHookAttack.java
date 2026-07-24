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
        zombie.setCol(8);
        int zRow = zombie.getRow();
        int zCol = zombie.getCol();

        Plant targetPlant = null;
        int rightmostCol = -1;

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile().getRow() == zRow && p.getPlacedTile().getCol() < zCol) {
                if (p.getPlacedTile().getCol() > rightmostCol) {
                    rightmostCol = p.getPlacedTile().getCol();
                    targetPlant = p;
                }
            }
        }

        if (targetPlant != null) {
            int pCol = targetPlant.getPlacedTile().getCol();

            if (pCol == zCol - 1) {

                targetPlant.takeDamage(99999);
                notify("Fisherman hooked and destroyed " + targetPlant.getName() + "!");
            } else {
                Tile oldTile = session.getArena().getTile(zRow, pCol);
                Tile newTile = session.getArena().getTile(zRow, pCol + 1);

                if (newTile.getPlants().isEmpty()) {
                    oldTile.getPlants().remove(targetPlant);

                    newTile.getPlants().add(targetPlant);

                    targetPlant.setPlacedTile(newTile);

                    notify("Fisherman pulled " + targetPlant.getName() + " to column " + (pCol + 1));
                }
            }
        }
    }
}
