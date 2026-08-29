package io.java.pvz.models.game.events;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.adventure.SeasonType;

public class GameEventPayload {
    private final GameEvent type;
    private final Arena arena;
    private final Zombie zombie;
    private final SeasonType seasonType;
    private final Plant plant;
    private final int amount;
    private final Position position;
    private final String message;
    private final ProjectileType projectileType;

    private GameEventPayload(Builder builder) {
        this.type = builder.type;
        this.arena = builder.arena;
        this.zombie = builder.zombie;
        this.seasonType = builder.seasonType;
        this.plant = builder.plant;
        this.amount = builder.amount;
        this.position = builder.position != null ?
            new Position(builder.position.getX(), builder.position.getY())
            : null;
        this.message = builder.message;
        this.projectileType = builder.projectileType;
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

    public String getMessage() {
        return message;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }

    public float getPixelX() {
        return position != null ? position.getX() : 0f;
    }

    public float getPixelY() {
        return position != null ? position.getY() : 0f;
    }


    public static class Builder {
        private final GameEvent type;

        private Arena arena;
        private Zombie zombie;
        private SeasonType seasonType;
        private Plant plant;
        private int amount = 0;
        private Position position;
        private String message;
        private ProjectileType projectileType;

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
            this.position = new Position(col, row);
            return this;
        }

        public Builder pixelCoordinate(float x, float y) {
            this.position = new Position(x, y);
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder projectileType(ProjectileType projectileType) {
            this.projectileType = projectileType;
            return this;
        }

        public GameEventPayload build() {
            return new GameEventPayload(this);
        }
    }
}
