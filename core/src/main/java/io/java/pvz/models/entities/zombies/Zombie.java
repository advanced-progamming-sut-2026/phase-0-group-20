package io.java.pvz.models.entities.zombies;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.entities.zombies.dismemberment.DismembermentData;
import io.java.pvz.models.entities.zombies.dismemberment.DismembermentLoader;
import io.java.pvz.models.entities.zombies.behavior.attack.AttackBehavior;
import io.java.pvz.models.entities.zombies.behavior.attack.HypnotizeAttack;
import io.java.pvz.models.entities.zombies.behavior.defense.DefenseBehavior;
import io.java.pvz.models.entities.zombies.behavior.effect.ChillEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.SunAbsorber;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.entities.zombies.behavior.move.MoveBehavior;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.net.NetworkIdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombie implements Ticker {
    public enum SpawnEffect {NORMAL, SANDSTORM, WATER_SPLASH, GRAVE_RISE}

    private static final Random RAND = new Random();
    private final List<Armor> armorPieces;
    private final List<ZombieEffect> activeEffects;
    private int waveCost;
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
    private float eatSpeedMultiplier = 1f;
    private ZombieType type;
    private ZombieState state = ZombieState.WALKING;
    private boolean canSpawnPlantFood;
    private int weight;
    private boolean attacking;
    private Tile tile;
    private SpawnEffect spawnEffect = SpawnEffect.NORMAL;
    private boolean isHypnotized = false;
    private Zombie targetZombie = null;
    private boolean shiny = false;
    private boolean burnedToAsh = false;
    private int smashDamage;
    private int armStagesRolled = 0;
    private int armStagesLost = 0;
    private int spawnTimer = 0;
    private int totalSpawnTicks = 0;

    private final Position position;

    protected String networkId;

    public Zombie(
        ZombieType type, ZombieData data, int row,
        MoveBehavior moveBehavior, AttackBehavior attackBehavior, DefenseBehavior defenseBehavior) {
        this.type = type;
        this.name = type.getJsonAlias();
        this.baseHp = data.getHitpoints();
        this.health = data.getHitpoints();
        this.baseSpeed = data.getSpeed() * PhysicalConstants.SPEED_SCALE_RATIO;
        this.currentSpeed = this.baseSpeed;
        this.eatDPS = data.getEatDps();
        this.waveCost = data.getWaveCost();
        this.position = new Position(9, row);
        this.smashDamage = data.getSmashDamage();
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

        if (isSpawning()) {
            spawnTimer--;
            if (spawnTimer <= 0) {
                state = ZombieState.WALKING;
                spawnEffect = SpawnEffect.NORMAL;
            }
            updateTile();
            return;
        }

        if (spawnEffect == SpawnEffect.SANDSTORM) {
            updateTile();
            return;
        }

        List<ZombieEffect> snapshot = new ArrayList<>(activeEffects);
        if (getCol() < 10) {
            for (ZombieEffect effect : snapshot) {
                effect.execute();
            }
        }

        if (attacking && getCol() < 10) {
            if (currentTick % 2 == 0) attackBehavior.execute();
        } else {
            moveBehavior.execute();
        }
        armorPieces.removeIf(Armor::isDropped);
        updateTile();
    }

    public void takeDamage(int damage, Projectile projectile) {
        if (dead || spawnEffect == SpawnEffect.SANDSTORM) return;

        if (projectile == null) {
            takeDamage(health);
            return;
        }

        ProjectileType projectileType = projectile.getType();
        boolean isFire = ProjectileType.isFireProjectile(projectileType);

        if (defenseBehavior != null && defenseBehavior.deflectProjectile(projectileType)) return;

        int remaining = damage;
        if (defenseBehavior != null) remaining = defenseBehavior.mitigateDamage(remaining, projectileType);
        if (remaining <= 0) return;

        if (isArmorBypassingProjectile(projectileType)) {
            applyHealthDamage(remaining, isFire);
            return;
        }

        takeDamageArmor(remaining, isFire);
    }

    public void takeDamage(int damage) {
        if (dead || spawnEffect == SpawnEffect.SANDSTORM) return;

        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            dead = true;
            if (this.getEffect() instanceof SunAbsorber sunAbsorber) {
                GameSession.getInstance().addSun(sunAbsorber.getSunAmount());
            }
        }
    }

    public void takeDamageArmor(int remaining, boolean isFire) {
        int remain = remaining;
        for (Armor a : armorPieces) {
            if (!a.isDestroyed()) {
                remain = a.takeDamage(remain);
                if (remain <= 0) return;
            }
        }
        applyHealthDamage(remain, isFire);
    }

    private boolean isArmorBypassingProjectile(ProjectileType projectileType) {
        return projectileType == ProjectileType.GOO_PEA;
    }

    private void applyHealthDamage(int remaining, boolean isFire) {
        this.health -= remaining;

        if (health <= 0) {
            health = 0;
            dead = true;
            if (isFire) this.burnedToAsh = true;
        }

        maybeLoseArm();
    }

    private void maybeLoseArm() {
        if (dead || baseHp <= 0) return;

        DismembermentData data = DismembermentLoader.getInstance().get(type.name());
        if (data == null || !data.hasArmStages()) return;

        List<DismembermentData.ArmStage> stages = data.getArmStages();
        float healthPercent = 100f * health / baseHp;

        while (armStagesRolled < stages.size()) {
            DismembermentData.ArmStage stage = stages.get(armStagesRolled);
            if (healthPercent > stage.getHealthPercent()) break;

            armStagesRolled++;
            if (RAND.nextInt(100) < stage.getChancePercent()) {
                armStagesLost = armStagesRolled;
            }
        }
    }

    public boolean isArmLost() {
        return armStagesLost > 0;
    }

    public int getArmStagesLost() {
        return armStagesLost;
    }

    public void hypnotize() {
        if (this.isHypnotized || this.dead) return;

        this.isHypnotized = true;
        if (this.getEffect() instanceof SunAbsorber sunAbsorber) {
            GameSession.getInstance().addSun(sunAbsorber.getSunAmount());
            sunAbsorber.setSunAmount(0);
        }

        attackBehavior = new HypnotizeAttack(this);
    }


    public void applyEatSpeedMultiplier(float multiplier) {
        this.eatSpeedMultiplier = multiplier;
    }

    public void resetEatSpeed() {
        this.eatSpeedMultiplier = 1f;
    }


    public void addArmor(Armor armor) {
        armorPieces.add(armor);
    }

    public void addEffect(ZombieEffect effect) {
        activeEffects.add(effect);
    }

    public void removeChillEffect() {
        activeEffects.removeIf(e -> e instanceof ChillEffect);
        resetSpeed();
    }

    public void removeFreezeEffect() {
        activeEffects.removeIf(e -> e instanceof FreezeEffect);
        resetSpeed();
    }

    protected void updateTile() {
        int column = this.getCol();
        int row = this.getRow();
        this.tile = GameSession.getInstance().getArena().getTile(row, column);
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

    public float getX() {
        return position.getX();
    }

    public void setX(float x) {
        position.setX(x);
    }

    public float getY() {
        return position.getY();
    }

    public void setY(float y) {
        position.setY(y);
    }

    public int getRow() {
        return position.getRow();
    }

    public boolean isOccupyingRow(int targetRow) {
        return this.position.getRow() == targetRow;
    }

    public void setRow(int row) {
        position.setRow(row);
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
        return (int) (eatDPS * eatSpeedMultiplier);
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

    public void move() {
        if (this.isHypnotized()) {
            this.position.moveX(Math.abs(this.currentSpeed));
            if (this.getCol() >= GameSession.getInstance().getArena().getCols() + 3) {
                this.setDead(true);
            }
        } else {
            this.position.moveX(-this.currentSpeed);
            if (this.getCol() > 12 && this.getCurrentSpeed() < 0)
                this.setDead(true);
            if (this.getCol() < -2) this.setDead(true);
        }
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getCol() {
        return position.getCol();
    }

    public void setCol(int col) {
        position.setCol(col);
        if (GameSession.getInstance() != null && this.networkId == null) {
            this.networkId = NetworkIdGenerator.generateZombieId(
                getType().name(), col, getRow(),
                GameSession.getInstance().getTimeManager().getCurrentTick()
            );
        }
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

    public Position getPosition() {
        return position;
    }

    public Zombie getTargetZombie() {
        return targetZombie;
    }

    public void setTargetZombie(Zombie targetZombie) {
        this.targetZombie = targetZombie;
    }

    public void setWaveCost(int waveCost) {
        this.waveCost = waveCost;
    }

    public boolean isShiny() {
        return shiny;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public boolean isBurnedToAsh() {
        return burnedToAsh;
    }

    public void setBurnedToAsh(boolean burnedToAsh) {
        this.burnedToAsh = burnedToAsh;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public int getSmashDamage() {
        return smashDamage;
    }

    public void setSmashDamage(int smashDamage) {
        this.smashDamage = smashDamage;
    }

    public void startSpawning(int ticks, SpawnEffect effect) {
        this.spawnTimer = ticks;
        this.totalSpawnTicks = ticks;
        this.spawnEffect = effect;
        this.state = ZombieState.INTRO;
    }

    public boolean isSpawning() {
        return spawnTimer > 0;
    }

    public int getSpawnTimer() {
        return spawnTimer;
    }

    public int getTotalSpawnTicks() {
        return totalSpawnTicks;
    }
}
