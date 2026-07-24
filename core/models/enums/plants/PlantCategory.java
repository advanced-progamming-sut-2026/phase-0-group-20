package models.enums.plants;

import java.util.Random;

public enum PlantCategory {
    SUN_PRODUCER,
    SHOOTER,
    LOBBER,
    EXPLOSIVE,
    MELEE,
    WALL_NUT,
    MODIFIER,
    STRIKE_THROUGH,
    HOMING;


    public static PlantCategory findByName(String name) {
        for (PlantCategory plantCategory : PlantCategory.values()) {
            if (plantCategory.name().equalsIgnoreCase(name)) {
                return plantCategory;
            }
        }
        return null;
    }

    public static PlantCategory getRandomPlantCategory() {
        PlantCategory[] categories = PlantCategory.values();
        int randomIndex = new Random().nextInt(categories.length);

        return categories[randomIndex];
    }

    public String getName() {
        return this.name().replaceAll("_", " ").toLowerCase();
    }
}
