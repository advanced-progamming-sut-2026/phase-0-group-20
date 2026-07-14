package models.entities.zombies.armour;

public class Armor {
    private final ArmorData data;
    private int currentHealth;
    private boolean dropped;

    public Armor(ArmorData data) {
        this.data = data;
        this.currentHealth = data.getBaseHealth();
        this.dropped = false;
    }

    public ArmorData getData() {
        return data;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public boolean isDropped() {
        return dropped;
    }

    public boolean isDestroyed() {
        return currentHealth <= 0;
    }

    public boolean isMetallic() {
        return getData().hasFlag("metallic");
    }

    public int takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            int overflow = Math.abs(currentHealth);
            currentHealth = 0;
            dropped = true;
            return overflow;
        }
        return 0;
    }

    public int getDamageLayer() {
        float ratio = (float) currentHealth / data.getBaseHealth();
        if (ratio > 0.666f) return 0;
        if (ratio > 0.333f) return 1;
        return 2;

    }

    @Override
    public String toString() {
        return data.getAlias() + "[" + currentHealth + "/" + data.getBaseHealth() + " layer=" + getDamageLayer() + "]";
    }
}
