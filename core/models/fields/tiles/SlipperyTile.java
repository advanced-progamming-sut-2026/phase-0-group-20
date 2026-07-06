package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

public class SlipperyTile extends Tile {

    private final SlideDirection direction;

    public SlipperyTile(int row, int col, SlideDirection direction) {
        super(row, col);
        this.direction = direction;
    }

    @Override
    public void onTick(int currentTick) {
        // nothing to do slide() is called when a zombie steps on this tile
    }

    public void slide(Zombie zombie) {
        if (zombie == null) return;

        int targetRow = (direction == SlideDirection.UP) ? row - 1 : row + 1;
        if (targetRow < 0 || targetRow >= GameSession.getInstance().getArena().getRows()) return;

        zombie.setRow(targetRow);
    }

    public SlideDirection getDirection() {
        return direction;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }

    public enum SlideDirection {UP, DOWN}
}
