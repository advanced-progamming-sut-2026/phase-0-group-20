package models.fields.tiles;

import models.entities.plants.Plant;
import models.fields.obstacle.GraveHolder;
import models.fields.obstacle.GraveStone;
import models.game.GameSession;

public class GraveStoneTile extends Tile implements GraveHolder {

    GraveStone graveStone = new GraveStone();

    public GraveStoneTile(int row, int col) {
        super(row, col);
    }

    public GraveStoneTile(int row, int col, GraveStone graveStone) {
        super(row, col);
        this.graveStone = graveStone;
    }

    @Override
    public void onTick(int currentTick) {
        // nothing to do
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }

    public GraveStone getGraveStone() {
        return graveStone;
    }

    @Override
    public void removeGrave() {
        GameSession.getInstance().getArena().changeTile(row, col, new NormalTile(row, col));
    }

}
