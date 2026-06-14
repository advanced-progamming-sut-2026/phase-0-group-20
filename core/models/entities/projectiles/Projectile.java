package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.timeManager.Ticker;

public class Projectile implements Ticker {

    private ProjectileType type;
    private int damage;
    private int column;
    private int lane;
    private int directionX;
    private int directionY;
    private boolean piercing;
    private boolean canPassObstacles; // for lobber

    @Override
    public void onTick(int currentTick) {
    }

    public void move() {
    }

    public void onHit(Zombie z) {
    }

    public void onHitObstacle(Tile tile) {
    }

    public boolean isOutOfBounds() {
        return false;
    }

}
