package com.Project.PVZ.models.fields.tiles;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.game.GameSession;

import java.util.Random;

public class RandomVaseTile extends Tile implements VaseTile {

    private static final Random RAND = new Random();
    private final VaseInside contents; // null = empty vase
    private boolean isBroken = false;

    public RandomVaseTile(int row, int col) {
        super(row, col);
        int random = RAND.nextInt(3);
        this.contents = (random == 0) ? null : (random == 1) ? VaseInside.ZOMBIE : VaseInside.SEED_PACKET;
    }

    @Override
    public void customTick(int currentTick) {

    }

    @Override
    public VaseInside breakVase() {
        isBroken = true;
        notify("Vase broken at [" + position.getRow() + "][" + position.getCol() + "]");
        GameSession.getInstance().getArena()
                .changeTile(position.getRow(), position.getCol(), new NormalTile(position.getRow(), position.getCol()));
        return contents;
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }

    @Override
    public boolean isBroken() {
        return isBroken;
    }
}
