package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.game.GameSession;

public class LowShoreTile extends Tile {
    private boolean isFlooded = false;
    private boolean isEmergeable = false;

    public LowShoreTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void customTick(int currentTick) {
        // nothing
    }

    public boolean canZombieEmerge() {
        return isFlooded && isEmergeable;
    }

    public boolean isEmergeable() {
        return isEmergeable;
    }

    public void setEmergeable(boolean emergeable) {
        isEmergeable = emergeable;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        boolean isWaterPlant = plantToPlant.getTags().contains(PlantTag.WATER);
        boolean isStackable = plantToPlant.getTags().contains(PlantTag.STACK);

        Plant topPlant = plants.isEmpty() ? null : plants.get(plants.size() - 1);

        if (isWaterPlant) return isFlooded && plants.isEmpty();

        if (isStackable)
            return (plants.isEmpty() && !isFlooded) ||
                    (!plants.isEmpty() && !topPlant.getTags().contains(PlantTag.STACK));

        if (plants.isEmpty())
            return !isFlooded;
        else
            return plants.size() == 1 && topPlant.getTags().contains(PlantTag.WATER);
    }

    public boolean isFlooded() {
        return isFlooded;
    }

    public void setFlooded(boolean flooded) {
        this.isFlooded = flooded;
    }
}
