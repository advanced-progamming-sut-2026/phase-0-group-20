package com.Project.PVZ.models.fields.tiles;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.fields.obstacle.GraveHolder;
import com.Project.PVZ.models.fields.obstacle.GraveStone;
import com.Project.PVZ.models.game.GameSession;

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
    public void customTick(int currentTick) {
        // nothing to do
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return this.getGraveStone() != null &&
                (plantToPlant.getName().contains("Buster")|| plantToPlant.getName().contains("buster"));
    }

    public GraveStone getGraveStone() {
        return graveStone;
    }

    @Override
    public void removeGrave() {
        GameSession.getInstance().getArena()
                .changeTile(position.getRow(), position.getCol(), new NormalTile(position.getRow(), position.getCol()));
    }

}
