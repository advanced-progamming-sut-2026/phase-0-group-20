package models.entities.plants;

import models.entities.plants.PlantFoodStrategy.PlantFoodStrategy;
import models.entities.plants.effect.PlantEffect;
import models.entities.plants.strategy.*;
import models.entities.plants.strategy.category_strategy.*;
import models.entities.plants.strategy.tag_strategy.ChargeStrategy;
import models.entities.plants.strategy.tag_strategy.TrapStrategy;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public class Plant implements IPlant, Ticker {
    protected final PlantData data;
    protected final List<IPlantStrategy> strategies = new ArrayList<>();
    protected int currentHp;
    protected Tile placedTile;
    protected int level = 1;
    protected int maxHp;
    protected int currentCost;
    protected float currentActionInterval;
    protected float currentRecharge;
    protected int bonusDamage = 0;

    protected final List<PlantFoodStrategy> plantFoodStrategy = new ArrayList<>();
    private int stackCount = 1;

    protected final List<PlantEffect> activeEffects = new ArrayList<>();
    protected boolean frozen = false;
    protected boolean stunned = false;

    protected int size = 1;
    protected boolean boosted = false;
    protected int boostTimer = 0;


    public Plant(PlantData data) {
        this.data = data;
        this.maxHp = data.baseHp();
        this.currentHp = this.maxHp;
        this.currentCost = data.cost();
        this.currentActionInterval = data.actionInterval();
        this.currentRecharge = data.recharge();
    }

    public void addStrategy(IPlantStrategy strategy) {
        this.strategies.add(strategy);
    }

    public List<IPlantStrategy> getStrategies() {
        return strategies;
    }

    public List<PlantFoodStrategy> getPlantFoodStrategy() {
        return plantFoodStrategy;
    }

    public void addPlantFoodStrategy(PlantFoodStrategy plantFoodStrategy) {
        this.plantFoodStrategy.add(plantFoodStrategy);
    }

    public void addEffect(PlantEffect effect) {
        activeEffects.add(effect);
        effect.apply(this);
    }

    public void useFood() {
        if (plantFoodStrategy.isEmpty()) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message(getName() + " has no Plant Food effect wired up yet!")
                            .build());
            return;
        }

        int maxDuration = 0;
        boolean isPermanentBoost = false;
        this.boosted = true;

        for (PlantFoodStrategy strategy : plantFoodStrategy) {
            strategy.reset();
            int duration = strategy.getDurationTicks();

            if (duration == -1)
                isPermanentBoost = true;
            else if (duration > maxDuration)
                maxDuration = duration;

            if (duration == 0 || duration == -1)
                strategy.executeStrategy(this);
        }

        if (maxDuration > 0)
            this.boostTimer = maxDuration;
        else if (isPermanentBoost)
            this.boostTimer = -1;
        else this.boosted = false;

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

        if (boostTimer > 0) {
            for (PlantFoodStrategy strategy : plantFoodStrategy)
                if (strategy.getDurationTicks() > 0)
                    strategy.executeStrategy(this);

            boostTimer--;

            if (boostTimer <= 0) boosted = false;

        } else
            for (IPlantStrategy strategy : strategies)
                strategy.execute(this, currentTick);

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

    public void die() {

    }


    @Override
    public int getCost() { return currentCost; }

    @Override
    public float getActionInterval() { return currentActionInterval; }

    @Override
    public float getRecharge() { return currentRecharge; }

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
    public int getBaseHp() {
        return data.baseHp();
    }

    @Override
    public int getDamage() {
        return data.damage();
    }

    @Override
    public String getAbilityType() {
        return data.abilityType();
    }

    @Override
    public float getAbilityValue() {
        return data.abilityValue();
    }

    @Override
    public String getPlantFoodType() {
        return data.plantFoodType();
    }

    @Override
    public float getPlantFoodValue() {
        return data.plantFoodValue();
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

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isStunned() {
        return stunned;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public List<PlantEffect> getActiveEffects() {
        return activeEffects;
    }

    public void upgrade() {
        if (this.level >= 4) return;

        this.level += 1;

        PlantUpgrade upgrade = this.data.upgrades().get(this.level);

        if (upgrade != null) {
            upgrade.type().apply(this, upgrade.value(), upgrade.specialTag());
        }

        // implement the effect of levels
    }

    public void applySpecialMechanic(String tag, float value) {
        switch (tag) {
            // sun production
            case "DOUBLE_SUN_CHANCE" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof SunProductionStrategy sunStrategy) {
                        sunStrategy.setDoubleSunChance(true);
                    }
                }
            }
            case "SUN_AMOUNT_BUFF" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof SunProductionStrategy sunProductionStrategy) {
                        sunProductionStrategy.increaseSunAmount(value);
                    }
                }
            }
            case "SUN_DROP_INCREMENT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof SunOnHitStrategy sunOnHitStrategy) {
                        sunOnHitStrategy.addSunPerHitMultiplier((int) value);
                    }
                }
            }
            // time and speed management
            case "GROW_TIME_REDUCTION" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof SunProductionStrategy sunStrategy) {
                        sunStrategy.reduceGrowTime(value);
                    }
                }
            }
            case "REGEN_SPEEDUP" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof ChargeStrategy regen) {
                        regen.speedUpRegen(value);
                    }
                }
            }
            case "EAT_TIME_REDUCTION" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof GraveBusterStrategy grave) {
                        grave.reduceEatTime(value);
                    }
                }
            }

            case "TILE_RANGE_EXT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof ShootingStrategy shooter) {
                        shooter.increaseRange((int) value);
                    } else if (s instanceof StrikeThroughStrategy strike) {
                        strike.increaseRange((int) value);
                    } else if (s instanceof MeleeStrategy melee) {
                        melee.increaseRange(value);
                    } else if (s instanceof MagnetStrategy magnet) {
                         magnet.increaseRange(value);
                    }
                }
            }
            case "SPLASH_DAMAGE_BUFF" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof LobberStrategy lobber) {
                        lobber.increaseSplashDamage((int) value);
                    }
                }
            }
            case "WARM_RADIUS_EXT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof LobberStrategy warm) {
                        warm.increaseWarmRadius(value);
                    }
                }
            }
            case "MELT_AREA_3X3" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof MeltIceStrategy melt) {
                        melt.setAreaOfEffect3x3(true);
                    }
                }
            }


            case "LIFESPAN_EXT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof LifespanStrategy decay) {
                        decay.increaseLifespan(value);
                    }
                }
            }
            case "CHILL_DURATION_EXT" -> { // its not completed because we need to add projectile effect
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof ShootingStrategy shoot) {
                        shoot.increaseChillDuration(value);
                    }
                }
            }
            case "FREEZE_DURATION_EXT" -> { // its not completed because we need to add freeze zombie
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof TrapStrategy freeze) {
                        freeze.increaseFreezeDuration(value);
                    } else if (s instanceof  GlobalEffectStrategy globalEffect) {
                        globalEffect.increaseFreezeDuration(value);
                    }
                }
            }
            case "DURATION_EXT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof MintBuffStrategy mint) {
                        mint.increaseBoostDuration(value);
                    }
                }
            }

            case "ADDITIONAL_PIERCE" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof StrikeThroughStrategy shoot) {
                        shoot.increasePierceLimit((int) value);
                    }
                }
            }
            case "POISON_TICK_BUFF" -> {
                for (IPlantStrategy s : this.strategies) { // it's not completed we need projectile effect
                    if (s instanceof ShootingStrategy shoot) {
                        shoot.increasePoisonTickDamage((int) value);
                    }
                }
            }
            case "PRIORITIZE_GARGANTUARS" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof HomingStrategy target) {
                        target.setPrioritizeGargantuars(true);
                    }
                }
            }
            case "BONUS_SMASH_CHARGES" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof TrapStrategy trap) {
                        trap.increaseSmashCharges((int) value);
                    }
                }
            }
            case "GRAPE_BOUNCE_EXT" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof ExplosiveStrategy explode) {
                        explode.increaseBounceLimit((int) value);
                    }
                }
            }
            case "BONUS_GRAB_TARGETS" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof TrapStrategy waterTrap) {
                        waterTrap.increaseMaxTargets((int) value);
                    }
                }
            }
            case "BUTTER_CHANCE_BUFF" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof LobberStrategy lobber) {
                        lobber.increaseButterChance(value);
                    }
                }
            }
            case "GROWTH_STAGE_MAX_UP" -> {
                this.setSize(this.getSize() + (int) value);
            }


            case "REFLECT_DAMAGE_BUFF" -> { // we dont have this strategy
            }
            case "EXPLODE_DAMAGE_BUFF" -> { // we dont have this strategy
            }
            case "DEATH_EXPLOSION_AOE" -> { // new strategy
//                this.addStrategy(new DeathExplosionStrategy((int) value));
            }
            case "EXPLODE_ON_FINISH" -> {
                for (IPlantStrategy s : this.strategies) {
                    if (s instanceof GraveBusterStrategy grave) {
                        grave.setExplodeOnFinish(true);
                    } else if (s instanceof MeltIceStrategy melt) {
                        melt.setExplodeOnFinish(true);
                    }
                }
            }

            case "ZOMBIE_HEALTH_MULTIPLIER" -> { // we dont have this strategy
            }
            case "ZOMBIE_DAMAGE_MULTIPLIER" -> { // we dont have this strategy
            }


            case "AUTO_PLANT_FOOD_CHANCE" -> { // new strategy
//                this.addStrategy(new AutoPlantFoodChanceStrategy(value));
            }
            case "AUTO_PLANTFOOD_ON_ENTER" -> { // new strategy
//                this.addStrategy(new AutoPlantFoodOnEnterStrategy());
            }
            case "RESET_FAMILY_COOLDOWNS" -> { // new strategy
//                this.addStrategy(new ResetFamilyCooldownStrategy(this.data.category()));
            }

            default -> System.out.println("Unhandled special mechanic: " + tag);
        }
    }

    public void onZombieDeath(Zombie z) {
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
                .zombie(z)
                .plant(this)
                .seasonType(GameSession.getInstance().getCurrentChapter().getSeasonType())
                .arena(GameSession.getInstance().getArena())
                .coordinate(z.getRow(),z.getCol())
                .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED, payload);
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getMaxSize() {
        return 3;
    }

    public int getBoostTimer() {
        return boostTimer;
    }

    public void setBoostTimer(int boostTimer) {
        this.boostTimer = boostTimer;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
    }
}
