package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.plants.strategy.MeltIceStrategy;
import models.fields.obstacle.IceBlock;
import models.fields.obstacle.IceHolder;

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
    public void onTick(int currentTick) {
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
