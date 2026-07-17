package test.java.models.entities.plants;

import models.entities.plants.Plant;
import models.entities.plants.PlantData;
import models.entities.plants.PlantFactory;
import models.entities.plants.PlantLoader;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PlantFactoryTest {
    private static final String JSON_PATH = "/home/elyas/Documents/GitHub/PVZ/core/resources/plants.json";

    @BeforeAll
    public static void setupFactory() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        List<PlantData> allPlants = PlantLoader.loadAll(JSON_PATH);

        Map<Integer, PlantData> registry = new HashMap<>();
        for (PlantData data : allPlants) {
            registry.put(data.id(), data);
        }

        new PlantFactory(registry);
    }

    static Stream<PlantData> provideAllPlantsData() {
        List<PlantData> loadedPlants = PlantLoader.loadAll(JSON_PATH);
        return loadedPlants.stream();
    }

    @ParameterizedTest(name = "Initial Stats Test for Plant: {0}")
    @MethodSource("provideAllPlantsData")
    public void testEverySinglePlantStats(PlantData expectedData) {
        assertNotNull(expectedData, "PlantData from JSON should not be null");

        Plant actualPlant = PlantFactory.create(expectedData.id());

        assertAll("Verifying initial stats for plant: " + expectedData.name(),
                () -> assertEquals(expectedData.name(), actualPlant.getName(), "Plant name mismatch"),
                () -> assertEquals(expectedData.baseHp(), actualPlant.getBaseHp(), "Base HP mismatch"),
                () -> assertEquals(expectedData.baseHp(), actualPlant.getCurrentHp(), "Plant should spawn with full HP"),
                () -> assertEquals(expectedData.cost(), actualPlant.getCost(), "Sun cost mismatch"),
                () -> assertEquals(expectedData.damage(), actualPlant.getDamage(), "Initial damage mismatch"),
                () -> assertEquals(expectedData.actionInterval(), actualPlant.getActionInterval(), "Action interval mismatch"),
                () -> assertEquals(expectedData.recharge(), actualPlant.getRecharge(), "Recharge time mismatch")
        );
    }
}
