package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.zombies.ZombieType;

public abstract class PushableObstacle implements Damageable {
    protected Position position;
    protected PushableObjectType type;
    protected int health;
    protected int maxHealth;
    protected boolean isDestroyed;

    public PushableObstacle(PushableObjectType type, int col, int row, int maxHealth) {
        this.type = type;
        this.position = new Position(col, row);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.isDestroyed = false;
    }

    @Override
    public void takeDamage(int damage) {
        if (isDestroyed) return;

        health -= damage;
        if (health <= 0) {
            health = 0;
            isDestroyed = true;
            onDestroy();
        }
    }

    public void push(float dx) {
        position.moveX(dx);
    }

    public boolean canBePushedBy(ZombieType zombieType) {
        ZombieType pusher = type.getPusherZombieType();
        return pusher == null || pusher == zombieType;
    }

    public abstract void onDestroy();

    public Position getPosition() {
        return position;
    }

    public float getX() {
        return position.getX();
    }

    public int getCol() {
        return position.getCol();
    }

    public int getRow() {
        return position.getRow();
    }

    public PushableObjectType getType() {
        return type;
    }

    @Override
    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
