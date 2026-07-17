package test.java.models.entities.plants;

import models.entities.plants.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlantUpgradeTest {
    private static final String JSON_PATH = "/home/elyas/Documents/GitHub/PVZ/core/resources/plants.json";

    @BeforeAll
    public static void setupFactory() {
        List<PlantData> allPlants = PlantLoader.loadAll(JSON_PATH);
        Map<Integer, PlantData> registry = new HashMap<>();
        for (PlantData data : allPlants) {
            registry.put(data.id(), data);
        }
        new PlantFactory(registry);
    }

    static Stream<PlantData> provideAllPlantsData() {
        return PlantLoader.loadAll(JSON_PATH).stream();
    }

    @ParameterizedTest(name = "Upgrade System Test for Plant: {0}")
    @MethodSource("provideAllPlantsData")
    public void testPlantUpgradesAppliedCorrectly(PlantData expectedData) {
        assertNotNull(expectedData, "PlantData from JSON should not be null");

        Plant actualPlant = PlantFactory.create(expectedData.id());

        // Ensure the plant starts at Level 1
        assertEquals(1, actualPlant.getLevel(), "Newly created plant should be Level 1");

        // Act & Assert - Loop through potential upgrades (Levels 2, 3, and 4)
        for (int targetLevel = 2; targetLevel <= 4; targetLevel++) {

            // Check if this plant has an upgrade defined for this specific level in JSON
            PlantUpgrade upgrade = expectedData.upgrades().get(targetLevel);
            if (upgrade == null) {
                continue;
            }

            // Save old stats to calculate relative differences for additions (like HP or Cost)
            int oldMaxHp = actualPlant.getMaxHp();
            int oldCost = actualPlant.getCost();
            int oldDamage = actualPlant.getDamage();
            float oldActionInterval = actualPlant.getActionInterval();
            float oldRecharge = actualPlant.getRecharge();

            actualPlant.upgrade();

            assertEquals(targetLevel, actualPlant.getLevel(), "Plant level did not increase properly");

            switch (upgrade.type()) {
                case BUFF_HP ->
                        assertEquals(oldMaxHp + (int) upgrade.value(), actualPlant.getMaxHp(),
                                "Max HP should increase by the upgrade value " + actualPlant.getName());

                case BUFF_DAMAGE ->
                        assertEquals((int) upgrade.value(), actualPlant.getBonusDamage(),
                                "Bonus damage should be applied exactly as the upgrade value "  + actualPlant.getName());

                case BUFF_COST ->
                        assertEquals(oldCost + (int) upgrade.value(), actualPlant.getCost(),
                                "Cost should be modified by the upgrade value " + actualPlant.getName());

                case BUFF_ACTION_INTERVAL ->
                        assertEquals(oldActionInterval + upgrade.value(), actualPlant.getActionInterval(), 0.001f,
                                "Action interval should decrease/increase by the upgrade value " + actualPlant.getName());

                case BUFF_RECHARGE ->
                        assertEquals(oldRecharge + upgrade.value(), actualPlant.getRecharge(), 0.001f,
                                "Recharge time should be modified by the upgrade value ");

                case SPECIAL_MECHANIC -> {
                    // For special mechanics, we verify that the plant method processed it without throwing exceptions.
                    // If you have specific flags (like isExplodeOnFinish), you can cast the plant and assert them here later.
                }
            }
        }
    }
}
