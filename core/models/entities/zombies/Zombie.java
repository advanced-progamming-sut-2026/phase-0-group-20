package models.entities.zombies;

import models.timeManager.Ticker;

import java.util.List;

public class Zombie implements Ticker {
    private String name;
    private ZombieType type;
    private int x;
    private int y;
    private int health;
    private List<ArmorType> armor;
    private float speed;

    @Override
    public void onTick(int currentTick) {

    }
}
