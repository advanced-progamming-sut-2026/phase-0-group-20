package io.java.pvz.models.entities.plants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.PlantFoodStrategy.PlantFoodStrategy;
import io.java.pvz.models.entities.plants.effect.FreezeEffect;
import io.java.pvz.models.entities.plants.effect.OctopusEffect;
import io.java.pvz.models.entities.plants.effect.PlantEffect;
import io.java.pvz.models.entities.plants.strategy.*;
import io.java.pvz.models.entities.plants.strategy.category_strategy.*;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.ChargeStrategy;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.TrapStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.BeghouledLevel;
import io.java.pvz.models.game.minigame.BeghouledManager;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Plant implements IPlant, Ticker {

    protected PlantData data;
    @JsonIgnore
    protected List<IPlantStrategy> strategies = new ArrayList<>();
    protected int currentHp;
    protected Position position;
    protected Tile placedTile;
    protected int level = 1;
    protected int maxHp;
    protected int currentCost;
    protected float currentActionInterval;
    protected float currentRecharge;
    protected int bonusDamage = 0;
    protected boolean dead = false;
    protected boolean asleep = false;

    protected List<PlantFoodStrategy> plantFoodStrategy = new ArrayList<>();
    private int stackCount = 1;

    protected List<PlantEffect> activeEffects = new ArrayList<>();
    protected boolean frozen = false;
    protected boolean stunned = false;

//    private int iceStacks = 0;
//    private int iceBlockHp = 0;
//    private static final int MAX_ICE_HP = 600;
//
//    private int octopusHp = 0;
//    private static final int MAX_OCTOPUS_HP = 800;

    protected int size = 1;
    protected boolean boosted = false;
    protected int boostTimer = 0;

    protected int actionTimer = 0;
    protected String currentAction = null;


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
        if (actionTimer > 0) actionTimer--;

        activeEffects.removeIf(effect -> {
            if (effect.isExpired()) {
                effect.remove(this);
                return true;
            }
            effect.execute(this, currentTick);
            return false;
        });

        if (stunned || isFrozen() || hasOctopus()) {
            return;
        }


        if (isAsleep()) {
            return;
        }


        if (boostTimer > 0) {
            for (PlantFoodStrategy strategy : plantFoodStrategy)
                if (strategy.getDurationTicks() > 0)
                    strategy.executeStrategy(this);

            boostTimer--;

            if (boostTimer <= 0) boosted = false;

        } else {
            for (IPlantStrategy strategy : strategies)
                strategy.execute(this, currentTick);
        }

    }

    public void takeDamage(int amount) {

        if (isDead()) {
            return;
        }

        this.currentHp -= amount;
        GameSession.notify(getName() + " Taking " + amount + " damage in " +
            (placedTile.getCol() + 1) + "," + (placedTile.getRow() + 1));

        if (currentHp <= 0) {
            this.currentHp = -1;
            this.dead = true;
        }

        if (isDead()) {
            if(GameSession.getInstance() != null
                && GameSession.getInstance().getCurrentMode() instanceof BeghouledLevel){
                if(placedTile != null)
                    placedTile.setCrater(true);
            }
            GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_LOST,
                new GameEventPayload.Builder(GameEvent.PLANT_LOST)
                    .message(getName() + " has lost!")
                    .build()
            );
            GameSession.getInstance().getTimeManager().unregisterTicker(this);
            GameSession.notify("Plant " + getName() + " has been Destroyed!");


        }
    }

    public int getStackCount() {
        return stackCount;
    }

    public boolean addStack() {
        if (this.getTags().contains(PlantTag.STACK) && stackCount < 5) {
            stackCount++;
            GameSession.notify("Plant " + getName() + " has been Stacked! stack count: " + stackCount);
            return true;
        }
        return false;
    }

    @Override
    public int getCost() {
        return currentCost;
    }

    @Override
    public float getActionInterval() {
        return currentActionInterval;
    }

    @Override
    public float getRecharge() {
        return currentRecharge;
    }

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
        position = new Position(placedTile.getCol(), placedTile.getRow());
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
    }

    public void applySpecialMechanic(String tag, float value) {
        if (applySunMechanic(tag, value)) return;
        if (applyTimeMechanic(tag, value)) return;
        if (applyRangeMechanic(tag, value)) return;
        if (applyDurationMechanic(tag, value)) return;
        if (applyOffensiveMechanic(tag, value)) return;
        if (applyStructuralMechanic(tag, value)) return;

        System.out.println("Unhandled special mechanic: " + tag);
    }

    private boolean applySunMechanic(String tag, float value) {
        switch (tag) {
            case "DOUBLE_SUN_CHANCE" -> applyToStrategy(SunProductionStrategy.class, s -> s.setDoubleSunChance(true));
            case "SUN_AMOUNT_BUFF" -> applyToStrategy(SunProductionStrategy.class, s -> s.increaseSunAmount(value));
            case "SUN_DROP_INCREMENT" ->
                applyToStrategy(SunOnHitStrategy.class, s -> s.addSunPerHitMultiplier((int) value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean applyTimeMechanic(String tag, float value) {
        switch (tag) {
            case "GROW_TIME_REDUCTION" -> applyToStrategy(SunProductionStrategy.class, s -> s.reduceGrowTime(value));
            case "REGEN_SPEEDUP" -> applyToStrategy(ChargeStrategy.class, s -> s.speedUpRegen(value));
            case "EAT_TIME_REDUCTION" -> applyToStrategy(GraveBusterStrategy.class, s -> s.reduceEatTime(value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean applyRangeMechanic(String tag, float value) {
        switch (tag) {
            case "TILE_RANGE_EXT" -> {
                applyToStrategy(ShootingStrategy.class, s -> s.increaseRange((int) value));
                applyToStrategy(StrikeThroughStrategy.class, s -> s.increaseRange((int) value));
                applyToStrategy(MeleeStrategy.class, s -> s.increaseRange(value));
                applyToStrategy(MagnetStrategy.class, s -> s.increaseRange(value));
            }
            case "SPLASH_DAMAGE_BUFF" ->
                applyToStrategy(LobberStrategy.class, s -> s.increaseSplashDamage((int) value));
            case "WARM_RADIUS_EXT" -> applyToStrategy(LobberStrategy.class, s -> s.increaseWarmRadius(value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean applyDurationMechanic(String tag, float value) {
        switch (tag) {
            case "LIFESPAN_EXT" -> applyToStrategy(LifespanStrategy.class, s -> s.increaseLifespan(value));
            case "CHILL_DURATION_EXT" -> applyToStrategy(ShootingStrategy.class, s -> s.increaseChillDuration(value));
            case "FREEZE_DURATION_EXT" -> {
                applyToStrategy(TrapStrategy.class, s -> s.increaseFreezeDuration(value));
                applyToStrategy(GlobalEffectStrategy.class, s -> s.increaseFreezeDuration(value));
            }
            case "DURATION_EXT" -> applyToStrategy(MintBuffStrategy.class, s -> s.increaseBoostDuration(value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean applyOffensiveMechanic(String tag, float value) {
        switch (tag) {
            case "ADDITIONAL_PIERCE" ->
                applyToStrategy(StrikeThroughStrategy.class, s -> s.increasePierceLimit((int) value));
            case "POISON_TICK_BUFF" ->
                applyToStrategy(ShootingStrategy.class, s -> s.increasePoisonTickDamage((int) value));
            case "PRIORITIZE_GARGANTUARS" -> applyToStrategy(HomingStrategy.class,
                s -> s.setTargetMode(HomingStrategy.TargetMode.GARGANTUAR_FIRST));
            case "BONUS_SMASH_CHARGES" -> applyToStrategy(TrapStrategy.class, s -> s.increaseSmashCharges((int) value));
            case "GRAPE_BOUNCE_EXT" ->
                applyToStrategy(ExplosiveStrategy.class, s -> s.increaseBounceLimit((int) value));
            case "BONUS_GRAB_TARGETS" -> applyToStrategy(TrapStrategy.class, s -> s.increaseMaxTargets((int) value));
            case "BUTTER_CHANCE_BUFF" -> applyToStrategy(LobberStrategy.class, s -> s.increaseButterChance(value));
            case "REFLECT_DAMAGE_BUFF" ->
                applyToStrategy(SpikeStrategy.class, s -> s.increaseReflectDamage((int) value));
            case "EXPLODE_DAMAGE_BUFF" ->
                applyToStrategy(DeathExplosionStrategy.class, s -> s.increaseExplosionDamage((int) value));
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean applyStructuralMechanic(String tag, float value) {
        switch (tag) {
            case "GROWTH_STAGE_MAX_UP" -> this.setSize(this.getSize() + (int) value);
            case "DEATH_EXPLOSION_AOE" ->
                applyToStrategy(TorchwoodStrategy.class, s -> s.setExplodesOnDeath(value > 0));
            case "EXPLODE_ON_FINISH" -> {
                applyToStrategy(GraveBusterStrategy.class, s -> s.setExplodeOnFinish(true));
                applyToStrategy(MeltIceStrategy.class, s -> s.setExplodeOnFinish(true));
            }
            case "ZOMBIE_HEALTH_MULTIPLIER" ->
                applyToStrategy(HypnotizeStrategy.class, s -> s.setHealthMultiplier(value));
            case "ZOMBIE_DAMAGE_MULTIPLIER" ->
                applyToStrategy(HypnotizeStrategy.class, s -> s.setDamageMultiplier(value));
            case "AUTO_PLANT_FOOD_CHANCE" ->
                applyToStrategy(ShootingStrategy.class, s -> s.setAutoPlantFoodChance(value));
            case "AUTO_PLANTFOOD_ON_ENTER" ->
                applyToStrategy(ImitateStrategy.class, s -> s.setAutoPlantFood(value > 0));
            case "RESET_FAMILY_COOLDOWNS" ->
                applyToStrategy(MintBuffStrategy.class, s -> s.setResetCooldowns(value > 0));
            case "MELT_AREA_3X3" -> applyToStrategy(MeltIceStrategy.class, s -> s.setMeltArea3x3(true));
            default -> {
                return false;
            }
        }
        return true;
    }

    private <T extends IPlantStrategy> void applyToStrategy(Class<T> strategyClass, Consumer<T> action) {
        for (IPlantStrategy s : this.strategies) {
            if (strategyClass.isInstance(s)) {
                action.accept(strategyClass.cast(s));
            }
        }
    }

    public void onZombieDeath(Zombie z) {
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
            .zombie(z)
            .plant(this)
            .seasonType(GameSession.getInstance().getCurrentChapter().getSeasonType())
            .arena(GameSession.getInstance().getArena())
            .coordinate(z.getRow(), z.getCol())
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isAsleep() {
        return asleep;
    }

    public void setAsleep(boolean asleep) {
        this.asleep = asleep;
    }


    public int getMaxHp() {
        return maxHp;
    }

    public int getBonusDamage() {
        return bonusDamage;
    }

    public boolean isDead() {
        return dead || currentHp < 0;
    }

    public void damageIceBlock(int damage) {
        FreezeEffect freezeEffect = getEffect(FreezeEffect.class);
        if (freezeEffect != null) {
            freezeEffect.takeDamage(this, damage);
        }
    }

    public void receiveIceHit() {
        if (isFrozen()) return;

        FreezeEffect freezeEffect = getEffect(FreezeEffect.class);
        if (freezeEffect != null) {
            freezeEffect.addStack(this);
        } else {
            addEffect(new FreezeEffect());
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean hasOctopus() {
        return getEffect(OctopusEffect.class) != null;
    }

    public void receiveOctopus() {
        if (hasOctopus() || isFrozen()) return;

        addEffect(new OctopusEffect(800));
    }

    public void damageOctopus(int damage) {
        OctopusEffect octopus = getEffect(OctopusEffect.class);
        if (octopus != null) {
            octopus.takeDamage(this, damage);
        }
    }

    public <T extends PlantEffect> T getEffect(Class<T> effectClass) {
        for (PlantEffect effect : activeEffects) {
            if (effectClass.isInstance(effect)) {
                return effectClass.cast(effect);
            }
        }
        return null;
    }

    public void triggerAction(String action) {
        this.currentAction = action;
        this.actionTimer = (int) (AnimationCatalog.getPlantAnimation(this).getDuration(action) * TimeManager.TICKS_PER_SECOND);
    }

    public String getCurrentAction() {
        return actionTimer > 0 ? currentAction : null;
    }

    public <T extends IPlantStrategy> T getStrategy(Class<T> strategyClass) {
        for (IPlantStrategy s : strategies)
            if (strategyClass.isInstance(s))
                return strategyClass.cast(s);
        return null;
    }
}
