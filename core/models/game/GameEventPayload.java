package models.game;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.adventure.SeasonType;

/**
 * The GameEventPayload class acts as the core of our Event-Driven architecture (Observer pattern).
 * Its main job is to serve as a Data Transfer Object (DTO) that carries event details
 * between different classes.
 * <p>
 * Without this class, our code would become tightly coupled. For example, imagine a zombie dies.
 * Several systems need to react:
 * 1. The Audio system needs to play a death sound.
 * 2. The UI system needs to remove the corpse.
 * 3. The Level Manager needs to check if the wave is complete.
 * 4. The Economy system needs to spawn loot at that specific location.
 * <p>
 * Without an event system, the Zombie class would have to directly call all these systems.
 * However, by using GameEventPayload, the Zombie class simply broadcasts a ZOMBIE_KILLED event
 * and passes a payload containing the necessary context. Any system that cares about this event
 * just listens for the payload and handles its own logic independently.
 */

public class GameEventPayload {
    private GameEvent type;
    private Zombie zombie;
    private SeasonType seasonType;
    private Plant plant;
    private int amount;
    private int row, col;

    public GameEventPayload(GameEvent type, Zombie zombie, Plant plant, SeasonType seasonType, int amount, int row, int col) {
        this.type = type;
        this.zombie = zombie;
        this.plant = plant;
        this.seasonType = seasonType;
        this.amount = amount;
        this.row = row;
        this.col = col;
    }

    public GameEvent getType() {
        return type;
    }

    public Zombie getZombie() {
        return zombie;
    }

    public Plant getPlant() {
        return plant;
    }

    public int getAmount() {
        return amount;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public SeasonType getSeasonType() {
        return seasonType;
    }
}
