package models.entities.plants;

import java.util.HashMap;
import java.util.Map;

public class PlantFactory {

    private final Map<Integer, PlantData> dataMap = new HashMap<>();

    public PlantFactory(String csvPath) throws Exception {
        for (PlantData pd : PlantLoader.loadAll(csvPath)) {
            dataMap.put(pd.id(), pd);
        }
    }

    public Plant create(int id) {
        return null; //For now
    }
}
