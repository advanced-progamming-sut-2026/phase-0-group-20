package models.entities.zombies;

import models.entities.plants.Plant;
import models.entities.zombies.armour.Armor;
import models.entities.zombies.behavior.attack.AttackBehavior;
import models.entities.zombies.behavior.attack.LaserAttack;
import models.entities.zombies.behavior.defense.DefenseBehavior;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.entities.zombies.behavior.effect.FreezeEffect;
import models.entities.zombies.behavior.effect.ZombieEffect;
import models.entities.zombies.behavior.move.MoveBehavior;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.game.events.GameEventMessenger;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombie implements Ticker {
    public enum SpawnEffect {NORMAL, SANDSTORM, WATER_SPLASH}

    private static final Random RAND = new Random();
    private final List<Armor> armorPieces;
    private final List<ZombieEffect> activeEffects;
    private final int waveCost;
    private String name;
    private MoveBehavior moveBehavior;
    private DefenseBehavior defenseBehavior;
    private AttackBehavior attackBehavior;
    private ZombieEffect effect;
    private int health;
    private int baseHp;
    private int eatDPS;
    private boolean dead;
    private float baseSpeed;
    private float currentSpeed;
    private ZombieType type;
    private ZombieState state = ZombieState.WALKING;
    private boolean canSpawnPlantFood;
    private int weight;
    private boolean attacking;
    private Tile tile;
    private SpawnEffect spawnEffect = SpawnEffect.NORMAL;
    private boolean isHypnotized = false;
    private GameEventMessenger messenger = GameEventMessenger.getInstance();

    private int row;
    private int col;
    private double x;

    public Zombie(ZombieType type, ZombieData data, int row, MoveBehavior moveBehavior, AttackBehavior attackBehavior, DefenseBehavior defenseBehavior) {
        this.type = type;
        this.name = type.getJsonAlias();
        this.baseHp = data.getHitpoints();
        this.health = data.getHitpoints();
        this.baseSpeed = data.getSpeed();
        this.currentSpeed = data.getSpeed();
        this.eatDPS = data.getEatDps();
        this.waveCost = data.getWaveCost();
        this.row = row;
        this.x = 10.0f;// initial state
        this.moveBehavior = moveBehavior;
        this.defenseBehavior = defenseBehavior;
        this.attackBehavior = attackBehavior;
        this.armorPieces = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.dead = false;
        this.attacking = false;
        this.canSpawnPlantFood = RAND.nextInt(100) < 10;
    }

    @Override
    public void onTick(int currentTick) {
        if (dead) return;

        List<ZombieEffect> toRemove = new ArrayList<>();
        for (ZombieEffect effect : activeEffects) {
            effect.execute();
        }
        activeEffects.removeAll(toRemove);

        if (attacking) {
            attackBehavior.execute();
        } else {
            moveBehavior.execute();
        }
        armorPieces.removeIf(Armor::isDropped);
    }

    public boolean takeDamage(int damage, ProjectileType projectileType) {
        if (dead) return false;

        if (projectileType == null) { // for lawn
            return applyHealthDamage(health);
        }

        if (defenseBehavior != null && defenseBehavior.deflectProjectile(projectileType)) {
            return false;
        }

        int remaining = damage;
        if (defenseBehavior != null) {
            remaining = defenseBehavior.mitigateDamage(remaining, projectileType);
        }
        if (remaining <= 0) return false;

        if (isArmorBypassingProjectile(projectileType)) {
            return applyHealthDamage(remaining);
        }

        for (int i = 0; i < armorPieces.size(); i++) {
            Armor a = armorPieces.get(i);
            if (!a.isDestroyed()) {
                remaining = a.takeDamage(remaining);
                if (remaining <= 0) return false;
                break;
            }
        }

        return applyHealthDamage(remaining);
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            dead = true;
        }
    }


    private boolean isArmorBypassingProjectile(ProjectileType projectileType) {
        return projectileType == ProjectileType.GOO_PEA;
    }

    private boolean applyHealthDamage(int remaining) {
        health -= remaining;
        if (health <= 0) {
            health = 0;
            dead = true;
            if (this.attackBehavior instanceof LaserAttack laserAttack) {
                int sunsToDrop = laserAttack.getStolenSuns() / 2;
                if (sunsToDrop > 0) {
                    System.out.println(this.getName() + " died and dropped " + sunsToDrop + " stolen suns!");
                }
            }
            return true;
        }
        return false;
    }


    public boolean takeDirectDamage(int damage) {
        if (dead) return false;

        health -= damage;
        if (health <= 0) {
            health = 0;
            dead = true;
            return true;
        }
        return false;
    }

    public boolean takeDirectDamage(int damage, Plant plant) { //implement harchi lazeme
        if (dead) return false;

        health -= damage;
        if (health <= 0) {
            health = 0;
            dead = true;
            return true;
        }
        return false;
    }

    public void hypnotize() {
        if (this.isHypnotized || this.dead) return;

        this.isHypnotized = true;
        this.currentSpeed = -Math.abs(this.baseSpeed);

        //implement new behavior for hypnotizing

        System.out.println(this.getName() + " has switched sides!");
    }

    private float eatSpeedMultiplier = 1f;

    public void applyEatSpeedMultiplier(float multiplier) {
        this.eatSpeedMultiplier = multiplier;
    }

    public void resetEatSpeed() {
        this.eatSpeedMultiplier = 1f;
    }

    public int getEffectiveEatDps() {
        return Math.round(eatDPS * eatSpeedMultiplier);
    }


    public void addArmor(Armor armor) {
        armorPieces.add(armor);
    }

    public void addEffect(ZombieEffect effect) {
        activeEffects.add(effect);
    }

    public void removeChillEffect() {
        activeEffects.removeIf(e -> e instanceof ChillEffect || e instanceof FreezeEffect);
        resetSpeed();
    }

    public void applySpeedMultiplier(float multiplier) {
        currentSpeed = baseSpeed * multiplier;
    }

    public void resetSpeed() {
        currentSpeed = baseSpeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZombieType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getHealth() {
        return health;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public int getEatDps() {
        return eatDPS;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public boolean canSpawnPlantFood() {
        return canSpawnPlantFood;
    }

    public List<Armor> getArmorPieces() {
        return armorPieces;
    }

    public List<ZombieEffect> getActiveEffects() {
        return activeEffects;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean v) {
        this.attacking = v;
    }

    public MoveBehavior getMoveBehavior() {
        return moveBehavior;
    }

    public void setMoveBehavior(MoveBehavior m) {
        this.moveBehavior = m;
    }

    public DefenseBehavior getDefenseBehavior() {
        return defenseBehavior;
    }

    public void setDefenseBehavior(DefenseBehavior defenseBehavior) {
        this.defenseBehavior = defenseBehavior;
    }

    public AttackBehavior getAttackBehavior() {
        return attackBehavior;
    }

    public void setAttackBehavior(AttackBehavior a) {
        this.attackBehavior = a;
    }

    public void setSpawnEffect(SpawnEffect effect) {
        this.spawnEffect = effect;
    }

    public SpawnEffect getSpawnEffect() {
        return spawnEffect;
    }

    public void moveForward() {
        this.x -= this.currentSpeed;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isCanSpawnPlantFood() {
        return canSpawnPlantFood;
    }

    public void setCanSpawnPlantFood(boolean canSpawnPlantFood) {
        this.canSpawnPlantFood = canSpawnPlantFood;
    }

    public ZombieState getState() {
        return state;
    }

    public void setState(ZombieState state) {
        this.state = state;
    }

    public void setType(ZombieType type) {
        this.type = type;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public int getEatDPS() {
        return eatDPS;
    }

    public void setEatDPS(int eatDPS) {
        this.eatDPS = eatDPS;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public ZombieEffect getEffect() {
        return effect;
    }

    public void setEffect(ZombieEffect effect) {
        this.effect = effect;
    }

    public boolean isHypnotized() {
        return isHypnotized;
    }
}
