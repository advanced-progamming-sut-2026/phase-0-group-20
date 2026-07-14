package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

public class PlantVaseTile extends Tile {
    public PlantVaseTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {

    }

    public VaseInside breakVase() {
        System.out.println("Vase broken at [" + position.getRow() + "][" + position.getCol() + "]");
        GameSession.getInstance().getArena().changeTile(position.getRow(),  position.getCol(), new NormalTile(position.getRow(),  position.getCol()));
        return VaseInside.SEED_PACKET;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
