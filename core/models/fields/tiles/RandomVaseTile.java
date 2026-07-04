package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.GameSession;

import java.util.Random;

public class RandomVaseTile extends Tile {
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

    public VaseInside breakVase() {
        System.out.println("Vase broken at [" + row + "][" + col + "]");
        GameSession.getInstance().getArena().changeTile(row, col, new NormalTile(row, col));
        return contents;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
