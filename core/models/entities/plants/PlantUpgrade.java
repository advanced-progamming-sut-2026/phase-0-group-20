package models.entities.plants;

public record PlantUpgrade(
        UpgradeType type,
        float value,
        String specialTag
) {}
