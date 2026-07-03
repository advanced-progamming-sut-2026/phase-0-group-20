package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public class Projectile implements Ticker {

    private ProjectileType type;
    private GameSession gameSession;
    private int damage;
    private int row;
    private int startCol;
    private int directionX;
    private int directionY;
    private boolean piercing;
    private boolean canPassObstacles; // for lobber

    private List<ProjectileEffect> effects;

    public Projectile(ProjectileType type, int damage, int row,int startCol, boolean canPassObstacles, GameSession gameSession) {
        this.type = type;
        this.damage = damage;
        this.row = row;
        this.startCol = startCol;
        this.canPassObstacles = canPassObstacles;
        this.gameSession = gameSession;
    }

    public void addEffect(ProjectileEffect effect) {
        if (effects == null) {
            effects = new ArrayList<>();
        }
        effects.add(effect);
    }

    @Override
    public void onTick(int currentTick) {
        for (ProjectileEffect effect : effects) {
            effect.applyEffect(gameSession);
        }
    }

    public void move() {
    }

    public void onHit(Zombie z) {
    }

    public void onHitObstacle(Tile tile) {
    }

    public boolean isOutOfBounds() {
        return false;
    }

    public ProjectileType getType() {
        return type;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public int getRow() {
        return row;
    }

    public int getDamage() {
        return damage;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getDirectionX() {
        return directionX;
    }

    public int getDirectionY() {
        return directionY;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public boolean isCanPassObstacles() {
        return canPassObstacles;
    }

    public List<ProjectileEffect> getEffects() {
        return effects;
    }
}
