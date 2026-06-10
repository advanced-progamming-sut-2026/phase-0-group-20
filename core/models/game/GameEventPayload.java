package models.game;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;

public class GameEventPayload {
    private GameEvent type;
    private Zombie zombie;
    private Plant plant;
    private int amount;
    private int row, col;
}
