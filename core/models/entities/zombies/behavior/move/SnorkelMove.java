package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.fields.tiles.Tile;
import models.game.GameSession;

public class SnorkelMove implements MoveBehavior {

    private final Zombie zombie;
    private boolean isSubmerged;

    public SnorkelMove(Zombie zombie) {
        this.zombie = zombie;
        this.isSubmerged = true;
    }

    @Override
    public void execute() {
        Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());
        boolean hasPlantToEat = currentTile != null && !currentTile.getPlants().isEmpty();
        isSubmerged = !hasPlantToEat;

        zombie.moveForward();  // under water
    }

    public boolean isSubmerged() {
        return isSubmerged;
    }

    public void setSubmerged(boolean submerged) {
        this.isSubmerged = submerged;
    }
}
