package io.java.pvz.models.entities.obstacle;

public interface Damageable {
    void takeDamage(int damage);
    int getHealth();
    boolean isDestroyed();
}
