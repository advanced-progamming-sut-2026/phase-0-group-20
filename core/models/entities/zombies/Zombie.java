package models.entities.zombies;

import models.timeManager.Ticker;

import java.util.List;

public class Zombie implements Ticker {
    private String name;
    private ZombieType type;
    private int x;
    private int y;
    private int baseHp; //for printing info in collectionMenu
    private int health;
    private List<ArmorType> armor;
    private float speed;

    @Override
    public void onTick(int currentTick) {

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

    public void setType(ZombieType type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<ArmorType> getArmor() {
        return armor;
    }

    public void setArmor(List<ArmorType> armor) {
        this.armor = armor;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getBaseHp() {
        return baseHp;
    }
}
