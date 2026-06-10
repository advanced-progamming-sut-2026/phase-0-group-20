package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;

import java.util.List;

import models.fields.Tile;
import models.timeManager.Ticker;

public abstract class Plant implements IPlant, Ticker {
    @Override
    public abstract void  onTick(int currentTick);

    protected int id;
    protected String name;
    protected int cost;
    protected int baseHp;
    protected int currentHp;
    protected int damage;
    protected Tile placedTile;
    protected int x;
    protected int y;

    protected PlantCategory category;
    protected List<PlantTag> tags;
    protected String baseAbility;


    @Override
    public int getId() {
        return id;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public PlantCategory getCategory() {
        return category;
    }


    @Override
    public List<PlantTag> getTags() {
        return tags;
    }


    @Override
    public int getCost() {
        return cost;
    }


    @Override
    public int getBaseHp() {
        return baseHp;
    }


    @Override
    public int getDamage() {
        return damage;
    }


    @Override
    public String getBaseAbility() {
        return baseAbility;
    }

}
