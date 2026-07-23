package models.entities.zombies;

import models.Position;
import models.entities.projectiles.Projectile;
import models.entities.zombies.armour.Armor;
import models.entities.zombies.behavior.attack.AttackBehavior;
import models.entities.zombies.behavior.attack.HypnotizeAttack;
import models.entities.zombies.behavior.attack.LaserAttack;
import models.entities.zombies.behavior.defense.DefenseBehavior;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.entities.zombies.behavior.effect.Effect;
import models.entities.zombies.behavior.effect.FreezeEffect;
import models.entities.zombies.behavior.effect.ZombieEffect;
import models.entities.zombies.behavior.move.MoveBehavior;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombie implements Ticker {
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
    private boolean hypnotized = false;
    private Zombie targetZombie = null;
    private boolean shiny = false;
    private Position position;

    public Zombie(ZombieType type, ZombieData data, int row, MoveBehavior moveBehavior, AttackBehavior attackBehavior, DefenseBehavior defenseBehavior) {
        this.type = type;
        this.name = type.getJsonAlias();
        this.baseHp = data.getHitpoints();
        this.health = data.getHitpoints();
        this.baseSpeed = data.getSpeed();
        this.currentSpeed = data.getSpeed();
        this.eatDPS = data.getEatDps();
        this.waveCost = data.getWaveCost();
        this.position = new Position(0, row);
        this.position.setX(10.0f);
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

        for (ZombieEffect effect : activeEffects) {
            effect.execute();
        }


        if (attacking) {
            attackBehavior.execute();
        } else {
            moveBehavior.execute();
        }
        armorPieces.removeIf(Armor::isDropped);
        updateTile();
    }

    public boolean takeDamage(int damage, Projectile projectile) {
        if (dead) return false;

        if (projectile == null) { // for lawn
            return applyHealthDamage(health);
        }

        ProjectileType projectileType = projectile.getType();

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
                notify(type.toString() + "'s armor take " + remaining + " in " + (position.getCol() + 1) + " " + (position.getRow() + 1));
                remaining = a.takeDamage(remaining);
                if (remaining <= 0) return false;
            }
        }

        return applyHealthDamage(remaining);
    }

    public boolean takeDamage(int damage) {
        this.health -= damage;
        notify(type.toString() + " take " + damage + " in " + (position.getCol() + 1) + " " + (position.getRow() + 1));
        if (this.health <= 0) {
            this.health = 0;
            dead = true;
        }
        return dead;
    }

    private boolean isArmorBypassingProjectile(ProjectileType projectileType) {
        return projectileType == ProjectileType.GOO_PEA;
    }

    private boolean applyHealthDamage(int remaining) {
        this.health -= remaining;
        notify(type.toString() + " take " + remaining + " in " + (position.getCol() + 1) + " " + (position.getRow() + 1));
        if (health <= 0) {
            health = 0;
            dead = true;
            if (this.attackBehavior instanceof LaserAttack laserAttack) {
                int sunsToDrop = laserAttack.getStolenSuns() / 2;
                if (sunsToDrop > 0) {
                    notify(this.getName() + " died and dropped " + sunsToDrop + " stolen suns!");
                }
            }
            return true;
        }
        return false;
    }

    public boolean takeDirectDamage(int damage) {
        if (dead) return false;

        this.health -= damage;
        notify(type.toString() + " take " + damage + " in " + (position.getCol() + 1) + " " + (position.getRow() + 1));
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

        attackBehavior = new HypnotizeAttack(this);

        notify(this.getName() + " has switched sides!");
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

    public void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }

    public String getInGameDetails() {
        StringBuilder info = new StringBuilder();

        info.append("Name: ").append(type.toString()).append("\n");

        info.append("position: ").append(position.getCol() + 1).append(", ").append(position.getRow() + 1).append("\n");

        info.append("health: ").append(this.health).append("\n");

        info.append("armor: ");
        if (armorPieces != null && !armorPieces.isEmpty()) {
            for (int i = 0; i < armorPieces.size(); i++) {
                info.append(armorPieces.get(i).getData().getAlias());
                if (i < armorPieces.size() - 1) info.append(", ");
            }
        }
        info.append("\n");

        info.append("effects:\n");
        if (activeEffects != null && !activeEffects.isEmpty()) {
            for (ZombieEffect e : activeEffects) {
                if (e instanceof Effect effectImpl) {

                    String effectName = effectImpl.getClass().getSimpleName().replace("Effect", "").toLowerCase();
                    if (effectName.equals("chill")) effectName = "chilled";
                    if (effectName.equals("freeze")) effectName = "frozen";

                    float remainingTime = effectImpl.getRemainingSeconds();
                    info.append(effectName).append(": ").append(String.format("%.1f", remainingTime)).append("s\n");
                }
            }
        }

        return info.toString().trim();
    }

    private void updateTile() {
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

    public void setType(ZombieType type) {
        this.type = type;
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

    public void setRow(int row) {
        position.setRow(row);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getEatDps() {
        return (int) (eatDPS * eatSpeedMultiplier);
    }

    public int getWaveCost() {
        return waveCost;
    }

    public void setWaveCost(int waveCost) {
        this.waveCost = waveCost;
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

    public void setDead(boolean dead) {
        this.dead = dead;
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

    public SpawnEffect getSpawnEffect() {
        return spawnEffect;
    }

    public void setSpawnEffect(SpawnEffect effect) {
        this.spawnEffect = effect;
    }

    public void moveForward() {
        if (this.isHypnotized()) {
            this.position.moveX(this.currentSpeed);

            if (this.getCol() >= GameSession.getInstance().getArena().getCols()) {
                this.setDead(true);
            }
        } else {
            this.position.moveX(-this.currentSpeed);
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

    public int getEatDPS() {
        return eatDPS;
    }

    public void setEatDPS(int eatDPS) {
        this.eatDPS = eatDPS;
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

    public void setHypnotized(boolean hypnotized) {
        this.hypnotized = hypnotized;
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

    public boolean isShiny() {
        return shiny;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public enum SpawnEffect {NORMAL, SANDSTORM, WATER_SPLASH}
}
