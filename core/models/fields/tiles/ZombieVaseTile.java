package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

public class ZombieVaseTile extends Tile {
    public ZombieVaseTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {

    }

    public VaseInside breakVase() {
        System.out.println("Vase broken at [" + row + "][" + col + "]");
        GameSession.getInstance().getArena().changeTile(row, col, new NormalTile(row, col));
        return VaseInside.ZOMBIE;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
