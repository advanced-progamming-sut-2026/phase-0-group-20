package models.entities.zombies;

import java.util.List;

public class ZombieData {
    private final String alias;
    private final int points;
    private final float speed;
    private final int eatDps;
    private final int waveCost;
    private final boolean canSpawnPlantFood;
    // Gargantuar-specific
    private final int smashDamage;
    private final String impType;

    private final List<String> armorProps;


    public ZombieData(String alias, int hitpoints, float speed, int eatDps,
                      int waveCost, boolean canSpawnPlantFood,
                      int smashDamage, String impType, List<String> armorProps) {
        this.alias = alias;
        this.points = hitpoints;
        this.speed = speed;
        this.eatDps = eatDps;
        this.waveCost = waveCost;
        this.canSpawnPlantFood = canSpawnPlantFood;
        this.smashDamage = smashDamage;
        this.impType = impType;
        this.armorProps = armorProps;
    }

    public String getAlias() {
        return alias;
    }

    public int getHitpoints() {
        return points;
    }

    public float getSpeed() {
        return speed;
    }

    public int getEatDps() {
        return eatDps;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public boolean canSpawnPlantFood() {
        return canSpawnPlantFood;
    }

    public int getSmashDamage() {
        return smashDamage;
    }

    public String getImpType() {
        return impType;
    }

    public List<String> getArmorProps() {
        return armorProps;
    }

    @Override
    public String toString() {
        return alias + " HP=" + points + " spd=" + speed + " dps=" + eatDps;
    }
}
