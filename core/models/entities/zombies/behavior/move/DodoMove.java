package models.entities.zombies.behavior.move;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.tiles.Tile;
import models.game.GameSession;

public class DodoMove implements MoveBehavior {
    private static final int HIGH_HP_OBSTACLE_THRESHOLD = 1000; // Wall-nut tier HP
    private final Zombie zombie;
    private final int FLY_DURATION_TICKS = 45; // it will change
    private boolean isFlying;
    private int flyTicks;

    public DodoMove(Zombie zombie) {
        this.zombie = zombie;
        this.isFlying = false;
        this.flyTicks = 0;
    }

    @Override
    public void execute() {
        if (isFlying) {
            zombie.moveForward(); // it can fly with another speed
            flyTicks++;

            if (flyTicks >= FLY_DURATION_TICKS) {
                isFlying = false;
            }
        } else {
            if (shouldFlyOverCurrentTile()) {
                startFlying();
            }
            zombie.moveForward();
        }
    }

    private boolean shouldFlyOverCurrentTile() {
        Tile tile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());
        if (tile == null || tile.getPlants().isEmpty()) {
            return false;
        }

        for (Plant plant : tile.getPlants()) {
            if (isTallNut(plant)) {
                return false; // Tall-nut always blocks flight, regardless of anything else on the tile
            }
        }
        for (Plant plant : tile.getPlants()) {
            if (isFlyableObstacle(plant)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFlyableObstacle(Plant plant) {
        String name = plant.getName();
        return plant.getCurrentHp() >= HIGH_HP_OBSTACLE_THRESHOLD
                || "WallNut".equalsIgnoreCase(name)
                || name.toLowerCase().contains("mine")
                || name.toLowerCase().contains("spikeweed")
                || name.toLowerCase().contains("magnetshroom"); //WARNING
    }

    private boolean isTallNut(Plant plant) {
        return "TallNut".equalsIgnoreCase(plant.getName());
    }

    public void startFlying() {
        if (!isFlying) {
            isFlying = true;
            flyTicks = 0;
        }
    }

    public boolean isFlying() {
        return isFlying;
    }
}
