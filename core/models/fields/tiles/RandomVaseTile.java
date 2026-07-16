package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

import java.util.Random;

public class RandomVaseTile extends Tile implements VaseTile {
    private static final Random RAND = new Random();

    private final VaseInside contents; // null = empty vase

    public RandomVaseTile(int row, int col) {
        super(row, col);
        int random = RAND.nextInt(3);
        this.contents = (random == 0) ? null : (random == 1) ? VaseInside.ZOMBIE : VaseInside.SEED_PACKET;
    }

    @Override
    public void onTick(int currentTick) {

    }

    @Override
    public VaseInside breakVase() {
        notify("Vase broken at [" + position.getRow() + "][" + position.getCol() + "]");
        GameSession.getInstance().getArena().changeTile(position.getRow(), position.getCol(), new NormalTile(position.getRow(), position.getCol()));
        return contents;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
