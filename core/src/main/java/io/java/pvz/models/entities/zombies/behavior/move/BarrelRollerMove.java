package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.obstacle.Barrel;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class BarrelRollerMove implements MoveBehavior {
    private final Zombie zombie;
    private final Barrel barrel;

    public BarrelRollerMove(Zombie zombie, Barrel barrel) {
        this.zombie = zombie;
        this.barrel = barrel;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        if (barrel != null && !barrel.isDestroyed()) {
            zombie.setAttacking(false);
            float oldX = zombie.getX();
            zombie.moveForward();
            float dx = zombie.getX() - oldX;

            barrel.push(dx);
            barrel.getPosition().setX(zombie.getX() - PhysicalConstants.TILE_WIDTH);
            crushPlantsOnTile(barrel);
        } else {
            zombie.moveForward();
        }
    }

    private void crushPlantsOnTile(Barrel barrel) {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<Plant> plantsToCrush = new ArrayList<>();

        Tile tile = session.getArena().getTile(barrel.getRow(), barrel.getCol());
        if (tile != null && !tile.getPlants().isEmpty()) {
            plantsToCrush.addAll(tile.getPlants());
        }

        for (Plant plant : session.getArena().getActivePlants()) {
            if (plant != null && !plant.isDead() && plant.getPlacedTile() != null) {
                if (plant.getPlacedTile().getRow() == barrel.getRow()) {
                    float dist = Math.abs(plant.getPosition().getX() - barrel.getX());
                    if (dist <= PhysicalConstants.TILE_WIDTH * 0.75f) {
                        if (!plantsToCrush.contains(plant)) {
                            plantsToCrush.add(plant);
                        }
                    }
                }
            }
        }

        for (Plant plant : plantsToCrush) {
            if (plant != null && !plant.isDead()) {
                plant.takeDamage(99999);
                if (plant.getPlacedTile() != null) {
                    plant.getPlacedTile().getPlants().remove(plant);
                }
                session.getArena().getActivePlants().remove(plant);
                session.getTimeManager().unregisterTicker(plant);
                GameSession.notify("Barrel crushed " + plant.getName() + "!");
            }
        }
    }

    public Barrel getBarrel() {
        return barrel;
    }
}
