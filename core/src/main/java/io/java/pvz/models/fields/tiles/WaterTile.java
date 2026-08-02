package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantTag;

public class WaterTile extends Tile {
    public WaterTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void customTick(int currentTick) {

    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        boolean isWaterPlant = plantToPlant.getTags().contains(PlantTag.WATER);
        boolean isStackable = plantToPlant.getTags().contains(PlantTag.STACK);

        Plant topPlant = plants.isEmpty() ? null : plants.get(plants.size() - 1);

        if (isWaterPlant) return plants.isEmpty();

        if (isStackable)
            return !plants.isEmpty() && !topPlant.getTags().contains(PlantTag.STACK);

        return plants.size() == 1 && topPlant.getTags().contains(PlantTag.WATER);
    }
}
