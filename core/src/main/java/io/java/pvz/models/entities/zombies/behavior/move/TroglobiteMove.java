package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.entities.obstacle.PushableObstacle;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class TroglobiteMove implements MoveBehavior {
    private final Zombie zombie;
    private IceBlock currentTargetIceBlock = null;

    public TroglobiteMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        if (currentTargetIceBlock != null && currentTargetIceBlock.isDestroyed()) {
            currentTargetIceBlock = null;
        }

        if (currentTargetIceBlock != null && zombie.getRow() != currentTargetIceBlock.getRow()) currentTargetIceBlock = null;

        if (currentTargetIceBlock == null) {
            currentTargetIceBlock = findIceBlockInFront();
        }

        if (currentTargetIceBlock != null && isCollidingWithIceBlock(currentTargetIceBlock)) {
            zombie.setState(ZombieState.PUSH);

            int oldCol = currentTargetIceBlock.getCol();

            float oldX = zombie.getX();
            zombie.move();
            float dx = zombie.getX() - oldX;

            currentTargetIceBlock.push(dx);
            int newCol = currentTargetIceBlock.getCol();

            if (oldCol != newCol) {
                Tile oldTile = session.getArena().getTile(currentTargetIceBlock.getRow(), oldCol);
                if (oldTile instanceof IceHolder oldHolder) {
                    if (oldHolder.getIceBlock() == currentTargetIceBlock) {
                        oldHolder.removeIceBlock();
                    }
                }

                Tile newTile = session.getArena().getTile(currentTargetIceBlock.getRow(), newCol);
                if (newTile instanceof IceHolder newHolder) {
                    newHolder.setIceBlock(currentTargetIceBlock);
                }

                crushPlantsOnTile(newCol, currentTargetIceBlock.getRow());
            }
        } else {
            currentTargetIceBlock = null;
            zombie.setState(ZombieState.WALKING);
            zombie.move();
        }
    }

    private IceBlock findIceBlockInFront() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return null;

        IceBlock closest = null;
        float minDistance = Float.MAX_VALUE;

        for (PushableObstacle obs : session.getArena().getActiveObstacles()) {
            if (obs instanceof IceBlock iceBlock && !iceBlock.isDestroyed()) {
                if (iceBlock.getRow() == zombie.getRow()) {
                    float dist = zombie.getX() - iceBlock.getX();
                    if (dist >= 10f && dist <= 120f && dist < minDistance) {
                        minDistance = dist;
                        closest = iceBlock;
                    }
                }
            }
        }
        return closest;
    }

    private boolean isCollidingWithIceBlock(IceBlock iceBlock) {
        if (iceBlock == null || iceBlock.isDestroyed()) return false;
        float dist = zombie.getX() - iceBlock.getX();
        return dist >= 10f && dist <= 110f;
    }

    private void crushPlantsOnTile(int col, int row) {
        GameSession session = GameSession.getInstance();
        Tile tile = session.getArena().getTile(row, col);
        if (tile != null && !tile.getPlants().isEmpty()) {
            List<Plant> plantsToCrush = new ArrayList<>(tile.getPlants());
            for (Plant plant : plantsToCrush) {
                if (!plant.isDead()) {
                    plant.takeDamage(99999);
                    tile.removePlant(plant);
                    GameSession.notify("IceBlock crushed " + plant.getName() + " on tile [" + (row + 1) + "," + (col + 1) + "]!");
                }
            }
        }
    }

    public IceBlock getCurrentTargetIceBlock() {
        return currentTargetIceBlock;
    }
}
