package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.Ticker;

public class Projectile implements Ticker {

    private ProjectileType type;
    private ProjectileEffect effect;
    private GameSession gameSession;
    private int damage;
    private int column;
    private int lane;
    private int speedX;
    private int speedY;
    private boolean piercing;
    private boolean canPassObstacles; // for lobber
    private boolean isDestroyed;

    public Projectile(ProjectileType type, ProjectileEffect effect, GameSession gameSession, int damage,
                      int column, int lane, int speedX, int speedY,
                      boolean piercing, boolean canPassObstacles) {
        this.type = type;
        this.effect = effect;
        this.gameSession = gameSession;
        this.damage = damage;
        this.column = column;
        this.lane = lane;
        this.speedX = speedX;
        this.speedY = speedY;
        this.piercing = piercing;
        this.canPassObstacles = canPassObstacles;
        this.isDestroyed = false;
    }

    @Override
    public void onTick(int currentTick) {
        if (isDestroyed) return;

        move();

        if (isOutOfBounds()) {
            isDestroyed = true;
        }
    }

    public void move() {
        column += speedX;
        lane += speedY;
    }

    public void onHit(Zombie z) {
        if (isDestroyed || z == null || z.isDead()) return;

        int finalDamage = damage * effect.getDamageMultiplier();

        if (effect.ignoresArmor()) {
            z.takeDirectDamage(finalDamage);
        } else {
            z.takeDamage(finalDamage);
        }

        effect.applyEffect(z, gameSession, this);

        if (!piercing) {
            isDestroyed = true;
        }
    }

    public void onHitObstacle(Tile tile) {
        if (!canPassObstacles) {
            isDestroyed = true;
        }
    }

    public boolean isOutOfBounds() {
        if (column < 0 || column >= gameSession.getArena().getCols()
                || lane < 0 || lane >= gameSession.getArena().getRows())
            return true;
        return false;
    }

    public ProjectileType getType() {
        return type;
    }

    public ProjectileEffect getEffect() {
        return effect;
    }

    public void setEffect(ProjectileEffect newEffect) {
        this.effect = newEffect;
    }

    public GameSession getBoard() {
        return gameSession;
    }

    public int getDamage() {
        return damage;
    }

    public int getColumn() {
        return column;
    }

    public int getLane() {
        return lane;
    }

    public int getspeedX() {
        return speedX;
    }

    public int getspeedY() {
        return speedY;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public boolean canPassObstacles() {
        return canPassObstacles;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }
}
