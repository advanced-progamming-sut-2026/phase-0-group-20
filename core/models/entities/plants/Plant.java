package models.entities.plants;

import models.entities.plants.PlantFoodStrategy.PlantFoodStrategy;
import models.entities.plants.effect.PlantEffect;
import models.entities.plants.strategy.IPlantStrategy;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public abstract class Plant implements IPlant, Ticker {
    protected final PlantData data;
    protected final List<IPlantStrategy> strategies = new ArrayList<>();
    protected int currentHp;
    protected Tile placedTile;
    protected int level = 1;
    protected PlantFoodStrategy plantFoodStrategy;
    protected GameSession gameSession;
    private int stackCount = 1;

    protected final List<PlantEffect> activeEffects = new ArrayList<>();
    protected boolean frozen = false;
    protected boolean stunned = false;


    public Plant(PlantData data, GameSession gameSession) {
        this.data = data;
        this.currentHp = data.baseHp();
        this.gameSession = gameSession;
    }

    public void addStrategy(IPlantStrategy strategy) {
        this.strategies.add(strategy);
    }

    public List<IPlantStrategy> getStrategies() {
        return strategies;
    }

    public PlantFoodStrategy getPlantFoodStrategy() {
        return plantFoodStrategy;
    }

    public void setPlantFoodStrategy(PlantFoodStrategy plantFoodStrategy) {
        this.plantFoodStrategy = plantFoodStrategy;
    }

    public void addEffect(PlantEffect effect) {
        activeEffects.add(effect);
        effect.apply(this);
    }

    /**
     * Called when the player feeds this plant with Plant Food.
     */
    public void useFood() {
        if (plantFoodStrategy != null) {
            plantFoodStrategy.executeStrategy(this);
        } else {
            System.out.println(getName() + " has no Plant Food effect wired up yet!");
        }
    }


    @Override
    public void onTick(int currentTick) {
        activeEffects.removeIf(effect -> {
            if (effect.isExpired()) {
                effect.remove(this);
                return true;
            }
            effect.execute(this, currentTick);
            return false;
        });

        if (stunned || frozen) {
            return;
        }

        for (IPlantStrategy strategy : strategies) {
            strategy.execute(this, currentTick, gameSession);
        }
    }

    public void takeDamage(int amount) {
        this.currentHp -= amount;
        if (this.currentHp <= 0) {
            die();
        }
    }

    public int getStackCount() {
        return stackCount;
    }

    public boolean addStack() {
        if (this.getName().equals("Pea Pod") && stackCount < 5) {
            stackCount++;
            return true;
        }
        return false;
    }

    protected abstract void die();

    @Override
    public int getId() {
        return data.id();
    }

    @Override
    public String getName() {
        return data.name();
    }

    @Override
    public PlantCategory getCategory() {
        return data.category();
    }

    @Override
    public List<PlantTag> getTags() {
        return data.tags();
    }

    @Override
    public int getCost() {
        return data.cost();
    }

    @Override
    public int getBaseHp() {
        return data.baseHp();
    }

    @Override
    public String getDamage() {
        return data.damage();
    }

    @Override
    public String getBaseAbility() {
        return data.baseAbility();
    }

    @Override
    public String getPlantFoodEffect() {
        return data.plantFoodEffect();
    }

    @Override
    public String getLvl2() {
        return data.lvl2();
    }

    @Override
    public String getLvl3() {
        return data.lvl3();
    }

    @Override
    public String getLvl4() {
        return data.lvl4();
    }

    @Override
    public float getActionInterval() {
        return data.actionInterval();
    }

    @Override
    public int getRecharge() {
        return data.recharge();
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public Tile getPlacedTile() {
        return placedTile;
    }

    public void setPlacedTile(Tile placedTile) {
        this.placedTile = placedTile;
    }

    public boolean isFrozen() { return frozen; }

    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public boolean isStunned() { return stunned; }

    public void setStunned(boolean stunned) { this.stunned = stunned; }

    public List<PlantEffect> getActiveEffects() { return activeEffects; }

    public void upgrade() {
        this.level += 1;
        // implement the effect of levels
    }
}
