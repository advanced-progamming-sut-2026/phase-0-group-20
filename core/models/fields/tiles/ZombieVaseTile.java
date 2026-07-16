package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

public class ZombieVaseTile extends Tile implements VaseTile {
    public ZombieVaseTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {

    }

    @Override
    public VaseInside breakVase() {
        notify("Vase broken at [" + position.getRow() + "][" + position.getCol() + "]");
        GameSession.getInstance().getArena().changeTile(position.getRow(), position.getCol(), new NormalTile(position.getRow(), position.getCol()));
        return VaseInside.ZOMBIE;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
