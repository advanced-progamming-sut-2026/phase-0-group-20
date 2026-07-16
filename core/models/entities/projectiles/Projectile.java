package models.entities.projectiles;

import models.Position;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.Ticker;

import java.util.List;
import java.util.Random;

public class Projectile implements Ticker {

    private final Plant plant;
    private ProjectileType type;
    private ProjectileEffect effect;
    private int damage;
    private Position position;
    private float speedX;
    private float speedY;
    private boolean piercing;
    private boolean canPassObstacles; // for lobber
    private boolean isDestroyed;

    private int size = 1;

    private Zombie target;
    private float baseSpeed;

    private int bouncesLeft = 0;
    private int lifespanTicks = -1;
    private int pierceCount = 999;

    public Projectile(Plant plant,
                      ProjectileType type,
                      ProjectileEffect effect,
                      int damage,
                      Position position,
                      float speedX,
                      float speedY,
                      boolean piercing,
                      boolean canPassObstacles) {
        this.plant = plant;
        this.type = type;
        this.effect = effect;
        this.damage = damage;
        this.position = position;
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


    public static Projectile spawnNewProjectile(Plant plant,
                                                ProjectileType type,
                                                int damage,
                                                Position position,
                                                int speedX,
                                                int speedY,
                                                boolean piercing,
                                                boolean canPassObstacles) {
        Projectile projectile = new Projectile(
                plant,
                type,
                projectileEffect(type),
                damage,
                position,
                speedX,
                speedY,
                piercing,
                canPassObstacles
        );
        GameSession.getInstance().getArena().addProjectile(projectile);
        return projectile;
    }


    private static ProjectileEffect projectileEffect(ProjectileType projectileType) {
        return switch (projectileType) {
            case PEA, ROTOBAGA_SEED -> new NormalEffect();
            case ICE_PEA -> new IceEffect();
            case FIRE_PEA -> new FireEffect();
            case GOO_PEA -> new PoisonProjectileEffect();
            default -> null;
        };
    }

    public void move() {
        if (target != null && !target.isDead()) {
            float dx = target.getX() - position.getX();
            float dy = target.getY() - position.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                speedX = (dx / distance) * baseSpeed;
                speedY = (dy / distance) * baseSpeed;
            }
        }

        position.moveX(speedX);
        position.moveY(speedY);

        if (bouncesLeft > 0) {
            boolean bounced = false;
            if (position.getX() >= GameSession.getInstance().getArena().getCols() && speedX > 0) {
                speedX = -speedX;
                bounced = true;
            } else if (position.getX() < 0 && speedX < 0) {
                speedX = -speedX;
                bounced = true;
            }

            if (position.getY() >= GameSession.getInstance().getArena().getRows() && speedY > 0) {
                speedY = -speedY;
                bounced = true;
            } else if (position.getY() < 0 && speedY < 0) {
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

        if (onHitBowlingMinigame(z)) return;

        int finalDamage = damage * effect.getDamageMultiplier();

        if (effect.ignoresArmor()) {
            boolean killed =z.takeDirectDamage(finalDamage);
            if(killed){
                this.getPlant().onZombieDeath(z);
            }
        } else {
            boolean killed = z.takeDamage(finalDamage, this);
            if(killed){
                this.getPlant().onZombieDeath(z);
            }
        }

        effect.applyEffect(z, this);

        if (piercing) {
            pierceCount--;
            if (pierceCount <= 0) {
                isDestroyed = true;
            }
        } else {
            isDestroyed = true;
        }
    }


    public boolean onHitBowlingMinigame(Zombie zombie) {

        Random  rand = new Random();

        if (type == ProjectileType.WALLNUT_BOWL) {
            zombie.takeDamage(damage);
            if (speedY == 0)
                speedY = rand.nextBoolean() ? speedX : -speedX;
            else
                speedY = -speedY;
            return true;

        } else if (type == ProjectileType.EXPLODE_NUT_BOWL) {
            System.out.println("Explode-o-nut bowled and exploded!");
            List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(position.getCol(), position.getRow(), 1.5);
            for (Zombie target : targets)
                if (!target.isDead()) target.takeDirectDamage(1800, plant);

            isDestroyed = true;
            return true;

        } else if (type == ProjectileType.GIANT_NUT_BOWL) {
            zombie.takeDirectDamage(zombie.getHealth(), plant);
            System.out.println("Giant nut crushed " + zombie.getName());
            return true;
        }

        return false;

    }

    public void onHitObstacle(Tile tile) {
        if (!canPassObstacles) {
            isDestroyed = true;
        }
    }

    public boolean isOutOfBounds() {
        if (bouncesLeft > 0) return false;
        return position.getX() < 0 || position.getX() >= GameSession.getInstance().getArena().getCols()
                || position.getY() < 0 || position.getY() >= GameSession.getInstance().getArena().getRows();
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

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getSpeedX() {
        return speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public float getSpeedXSpeed() {
        return speedX;
    }

    public float getSpeedYSpeed() {
        return speedY;
    }

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Zombie getTarget() {
        return target;
    }

    public void setTarget(Zombie target) {
        this.target = target;
    }

    public Plant getPlant() {
        return plant;
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
