package models.game.adventure.levels.speciallevels;

import models.entities.plants.Plant;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

import java.util.ArrayList;
import java.util.List;

public class LockedPlants extends SpecialLevel {
    private final List<Plant> lockedPlants;
    private final List<Plant> forcedToUsePlants;

    public LockedPlants(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber
            , List<Plant> lockedPlants, List<Plant> forcedToUsePlants) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.forcedToUsePlants = forcedToUsePlants != null ? forcedToUsePlants : new ArrayList<>();
        this.lockedPlants = lockedPlants != null ? lockedPlants : new ArrayList<>();
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        // nothing to add
    }

    @Override
    public boolean isPlantAllowed(Plant plant) {
        return lockedPlants.stream()
                .noneMatch(plant1 -> plant1.getName().equals(plant.getName()));
    }

    public List<Plant> getLockedPlants() {
        return lockedPlants;
    }

    public List<Plant> getForcedToUsePlants() {
        return forcedToUsePlants;
    }
}
