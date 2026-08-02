package com.Project.PVZ.models.fields.tiles;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.strategy.MeltIceStrategy;
import com.Project.PVZ.models.fields.obstacle.IceBlock;
import com.Project.PVZ.models.fields.obstacle.IceHolder;

public class NormalTile extends Tile implements IceHolder {

    private IceBlock iceBlock = null;

    public NormalTile(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        if (hasIceBlock())
            return plantToPlant.getStrategies().stream()
                    .anyMatch(strategy -> strategy instanceof MeltIceStrategy);

        return super.isPlantable(plantToPlant);
    }

    @Override
    public void customTick(int currentTick) {
    }

    @Override
    public boolean hasIceBlock() {
        return iceBlock != null;
    }

    @Override
    public IceBlock getIceBlock() {
        return iceBlock;
    }

    @Override
    public void setIceBlock(IceBlock iceBlock) {
        this.iceBlock = iceBlock;
    }

    @Override
    public void removeIceBlock() {
        this.iceBlock = null;
    }

}
