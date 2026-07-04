package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

public class GraveStoneTile extends Tile {

    GraveStone graveStone = new GraveStone();

    public GraveStoneTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {
        // nothing to do
    }

    public void takeDamage(int damage) {
        graveStone.takeDamage(damage);

        if (graveStone.getHp() <= 0) {
            System.out.println("Grave destroyed at row: " + row + ", col: " + col);
            GameSession.getInstance().getArena().changeTile(row, col, new NormalTile(row, col));
        }
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
