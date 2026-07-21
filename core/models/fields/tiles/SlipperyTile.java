package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class SlipperyTile extends Tile {

    private final SlideDirection direction;

    public SlipperyTile(int row, int col, SlideDirection direction) {
        super(row, col);
        this.direction = direction;
    }

    @Override
    public void onTick(int currentTick) {
        // nothing to do slide() is called when a zombie steps on this tile
        System.out.println("asdfasdfsdf");

        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<Zombie> zombiesOnTile = session.getArena().getZombiesOnTile(this);


        for (Zombie zombie : zombiesOnTile)
            if (zombie != null && !zombie.isDead()) {
                slide(zombie);
            }
    }

    public void slide(Zombie zombie) {
        if (zombie == null) return;

        int targetRow = (direction == SlideDirection.UP) ? position.getRow() - 1 : position.getRow() + 1;
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
