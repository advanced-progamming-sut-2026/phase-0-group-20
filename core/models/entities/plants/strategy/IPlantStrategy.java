package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.game.GameSession;

public interface IPlantStrategy {
    void execute(Plant context, int currentTick, GameSession gameSession);
}
