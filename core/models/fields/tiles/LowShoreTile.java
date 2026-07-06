package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantTag;
import models.game.GameSession;

public class LowShoreTile extends Tile {
    private boolean isFlooded = false;

    public LowShoreTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {
        // nothing
    }

    public boolean canZombieEmerge() {
        return isFlooded;
    }

    public void spawnZombieFromBelow(Zombie zombie) {
        if (zombie == null) return;

        zombie.setRow(row);
        zombie.setX(col);
        GameSession session = GameSession.getInstance();
        session.getArena().addZombie(zombie);
        session.getTimeManager().registerNewTicker(zombie);
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