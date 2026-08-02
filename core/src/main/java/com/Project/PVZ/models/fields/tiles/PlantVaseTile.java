package com.Project.PVZ.models.fields.tiles;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.game.GameSession;

public class PlantVaseTile extends Tile implements VaseTile {

    private boolean isBroken = false;

    public PlantVaseTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void customTick(int currentTick) {

    }

    @Override
    public VaseInside breakVase() {
        isBroken = true;
        notify("Vase broken at [" + (position.getCol()+1) + "][" + (position.getRow()+1) + "]");
        GameSession.getInstance().getArena().changeTile(position.getRow(), position.getCol(),
                new NormalTile(position.getRow(), position.getCol()));
        return VaseInside.SEED_PACKET;
    }

    @Override
    public boolean isBroken() {
        return isBroken;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
