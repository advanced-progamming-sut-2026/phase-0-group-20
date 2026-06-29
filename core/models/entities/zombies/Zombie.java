package models.entities.zombies;

import models.entities.zombies.armour.Armor;
import models.entities.zombies.behavior.attack.AttackBehavior;
import models.entities.zombies.behavior.defense.DefenseBehavior;
import models.entities.zombies.behavior.effect.ZombieEffect;
import models.entities.zombies.behavior.move.MoveBehavior;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombie implements Ticker {

    private static final Random RAND = new Random();
    private String name;
    private MoveBehavior moveBehavior;
    private DefenseBehavior defenseBehavior;
    private AttackBehavior attackBehavior;
    private ZombieEffect effect;

    private final List<Armor> armorPieces;
    private final List<ZombieEffect> activeEffects;

    private int health;
    private int baseHp;
    private int eatDPS;
    private boolean dead;
    private float baseSpeed;
    private float currentSpeed;
    private final int waveCost;
    private ZombieType type;
    private ZombieState state = ZombieState.WALKING;
    private boolean canSpawnPlantFood;
    private int weight;
    private boolean attacking;

    private int row;
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
        this.x = -1; // initial state
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
    }

    public boolean takeDamage(int damage) {
        if (dead) return false;

        int remaining = damage;

        for (int i = 0; i < armorPieces.size(); i++) {
            Armor a = armorPieces.get(i);
            if (!a.isDestroyed()) {
                remaining = a.takeDamage(remaining);
                if (remaining <= 0) return false;
                break;
            }
        }

        health -= remaining;
        if (health <= 0) {
            health = 0;
            dead = true;
            return true;
        }
        return false;
    }

    public void addArmor(Armor armor) {
        armorPieces.add(armor);
    }

    public void addEffect(ZombieEffect effect) {
        activeEffects.add(effect);
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

    public void setX(int x) {
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

    public DefenseBehavior getDefenseBehavior() {
        return defenseBehavior;
    }

    public void setDefenseBehavior(DefenseBehavior defenseBehavior) {
        this.defenseBehavior = defenseBehavior;
    }

    public void setMoveBehavior(MoveBehavior m) {
        this.moveBehavior = m;
    }

    public AttackBehavior getAttackBehavior() {
        return attackBehavior;
    }

    public void setAttackBehavior(AttackBehavior a) {
        this.attackBehavior = a;
    }


    public void moveForward() {
        this.x -= this.currentSpeed;
    }
}
