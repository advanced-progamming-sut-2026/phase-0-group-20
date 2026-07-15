package models.game.events;

import models.Position;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.Arena;
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
    private final GameEvent type;
    private final Arena arena;
    private final Zombie zombie;
    private final SeasonType seasonType;
    private final Plant plant;
    private final int amount;
    private final Position position;

    private GameEventPayload(Builder builder) {
        this.type = builder.type;
        this.arena = builder.arena;
        this.zombie = builder.zombie;
        this.seasonType = builder.seasonType;
        this.plant = builder.plant;
        this.amount = builder.amount;
        this.position = builder.position != null ? new Position(builder.position.getCol(), builder.position.getRow()) : null;
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
        return position.getRow();
    }

    public int getCol() {
        return position.getCol();
    }

    public SeasonType getSeasonType() {
        return seasonType;
    }

    public Arena getArena() {
        return arena;
    }


    public static class Builder {
        private final GameEvent type;

        private Arena arena;
        private Zombie zombie;
        private SeasonType seasonType;
        private Plant plant;
        private int amount = 0;
        private Position position;

        public Builder(GameEvent type) {
            this.type = type;
        }

        public Builder arena(Arena arena) {
            this.arena = arena;
            return this;
        }

        public Builder zombie(Zombie zombie) {
            this.zombie = zombie;
            return this;
        }

        public Builder seasonType(SeasonType seasonType) {
            this.seasonType = seasonType;
            return this;
        }

        public Builder plant(Plant plant) {
            this.plant = plant;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder coordinate(int row, int col) {
            position.setRow(row);
            position.setCol(col);
            return this;
        }

        public GameEventPayload build() {
            return new GameEventPayload(this);
        }
    }
}