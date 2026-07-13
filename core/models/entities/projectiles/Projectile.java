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
    private double x;
    private double y;
    private double speedX;
    private double speedY;
    private boolean piercing;
    private boolean canPassObstacles; // for lobber
    private boolean isDestroyed;

    private Zombie target;
    private float baseSpeed;

    private int bouncesLeft = 0;
    private int lifespanTicks = -1;
    private int pierceCount = 999;

    public Projectile(ProjectileType type, ProjectileEffect effect, GameSession gameSession, int damage,
                      double x, double y, double speedX, double speedY,
                      boolean piercing, boolean canPassObstacles) {
        this.type = type;
        this.effect = effect;
        this.gameSession = gameSession;
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.piercing = piercing;
        this.canPassObstacles = canPassObstacles;
        this.isDestroyed = false;
    }

    @Override
    public void onTick(int currentTick) {
        if (isDestroyed) return;

        if (lifespanTicks > 0) {
            lifespanTicks--;
            if (lifespanTicks == 0) {
                isDestroyed = true;
                return;
            }
        }

        move();

        if (isOutOfBounds()) {
            isDestroyed = true;
        }
    }

    public void move() {
        if (target != null && !target.isDead()) {
            double dx = target.getX() - x;
            double dy = target.getRow() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                speedX = (dx / distance) * baseSpeed;
                speedY = (dy / distance) * baseSpeed;
            }
        }

        x += speedX;
        y += speedY;

        if (bouncesLeft > 0) {
            boolean bounced = false;
            if (x >= gameSession.getArena().getCols() && speedX > 0) {
                speedX = -speedX;
                bounced = true;
            } else if (x < 0 && speedX < 0) {
                speedX = -speedX;
                bounced = true;
            }

            if (y >= gameSession.getArena().getRows() && speedY > 0) {
                speedY = -speedY;
                bounced = true;
            } else if (y < 0 && speedY < 0) {
                speedY = -speedY;
                bounced = true;
            }

            if (bounced) bouncesLeft--;
        }
    }

    public void setHomingTarget(Zombie target, float baseSpeed) {
        this.target = target;
        this.baseSpeed = baseSpeed;
    }

    public void onHit(Zombie z) {
        if (isDestroyed || z == null || z.isDead()) return;

        int finalDamage = damage * effect.getDamageMultiplier();

        if (effect.ignoresArmor()) {
            z.takeDirectDamage(finalDamage);
        } else {
            z.takeDamage(finalDamage, type);
        }

        effect.applyEffect(z, gameSession, this);

        if (piercing) {
            pierceCount--;
            if (pierceCount <= 0) {
                isDestroyed = true;
            }
        } else {
            isDestroyed = true;
        }
    }

    public void onHitObstacle(Tile tile) {
        if (!canPassObstacles) {
            isDestroyed = true;
        }
    }

    public boolean isOutOfBounds() {
        if (bouncesLeft > 0) return false;
        return x < 0 || x >= gameSession.getArena().getCols()
                || y < 0 || y >= gameSession.getArena().getRows();
    }

    public ProjectileType getType() {
        return type;
    }

    public void setType(ProjectileType type) {
        this.type = type;
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

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getspeedX() {
        return speedX;
    }

    public double getspeedY() {
        return speedY;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPierceCount(int count) {
        this.pierceCount = count;
    }

    public boolean canPassObstacles() {
        return canPassObstacles;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setBouncesLeft(int bounces) {
        this.bouncesLeft = bounces;
    }

    public void setLifespanTicks(int ticks) {
        this.lifespanTicks = ticks;
    }
}
