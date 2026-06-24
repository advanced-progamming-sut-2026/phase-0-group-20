package models.entities.plants;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.fields.tiles.Tile;
import models.timeManager.Ticker;

import java.util.List;

public abstract class Plant implements IPlant, Ticker {
    @Override
    public abstract void onTick(int currentTick);

    protected int id;
    protected String name;
    protected int cost;
    protected int baseHp;
    protected int currentHp;
    protected int damage;
    protected Tile placedTile;
    protected int level =1;
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

    public int getLevel() {
        return level;
    }

    public void upgrade() {
        this.level +=1;
        // later we implement the effects
    }


}
