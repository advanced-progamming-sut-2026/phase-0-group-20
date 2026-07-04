package models.fields.tiles;

import models.entities.plants.Plant;
import models.enums.plants.PlantTag;

public class WaterTile extends Tile {
    public WaterTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {

    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        boolean isWaterPlant = plantToPlant.getTags().contains(PlantTag.WATER);
        boolean isStackable = plantToPlant.getTags().contains(PlantTag.STACK);

        Plant topPlant = plants.isEmpty() ? null : plants.get(plants.size() - 1);

        if (isStackable)
            return !plants.isEmpty() && !topPlant.getTags().contains(PlantTag.STACK);


        if (isWaterPlant) return plants.isEmpty();

        return plants.size() == 1 && topPlant.getTags().contains(PlantTag.WATER);
    }
}
