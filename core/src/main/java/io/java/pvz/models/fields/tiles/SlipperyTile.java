package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.SlideCooldownEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.SlideEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.entities.zombies.zomboss.Zomboss;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;

import java.util.List;

public class SlipperyTile extends Tile {

    private final SlideDirection direction;

    public SlipperyTile(int row, int col, SlideDirection direction) {
        super(row, col);
        this.direction = direction;
    }

    @Override
    public void customTick(int currentTick) {

        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<Zombie> zombiesOnTile = session.getArena().getZombiesOnTile(this);


        for (Zombie zombie : zombiesOnTile) {
            if (zombie != null && !zombie.isDead()) {
                boolean canSlide = true;
                for (ZombieEffect e : zombie.getActiveEffects()) {
                    if (e instanceof SlideEffect || e instanceof SlideCooldownEffect) {
                        canSlide = false;
                        break;
                    }
                }

                float tileCenterX = (position.getCol() * PhysicalConstants.TILE_WIDTH) +
                    PhysicalConstants.GRID_START_X + (PhysicalConstants.TILE_WIDTH / 2f);
                float dx = Math.abs(zombie.getX() - tileCenterX);

                if (canSlide && dx < 30f)
                    slide(zombie);

            }
        }
    }

    public void slide(Zombie zombie) {
        if (zombie == null || zombie instanceof Zomboss) return;

        int targetRow = (direction == SlideDirection.UP) ? position.getRow() - 1 : position.getRow() + 1;
        if (targetRow < 0 || targetRow >= GameSession.getInstance().getArena().getRows()) return;

        zombie.addEffect(new SlideEffect(zombie, targetRow));
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
