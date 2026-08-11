package io.java.pvz.models.entities.zombies.armour;

import java.util.List;

public class ArmorData {
    private final String alias;
    private final String armorType;
    private final int baseHealth;
    private final List<String> flags;
    private final List<String> armorLayers;

    public ArmorData(String alias, String armorType, int baseHealth, List<String> flags, List<String> armorLayers) {
        this.alias = alias;
        this.armorType = armorType;
        this.baseHealth = baseHealth;
        this.flags = flags;
        this.armorLayers = armorLayers;
    }

    public String getAlias() {
        return alias;
    }

    public String getArmorType() {
        return armorType;
    }

    public int getBaseHealth() {
        return baseHealth;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public List<String> getFlags() {
        return flags;
    }

    public String getArmorLayer(int index) {
        return armorLayers.get(index);
    }

    @Override
    public String toString() {
        return alias + " (Type: " + armorType + ", HP: " + baseHealth + ")";
    }
}
