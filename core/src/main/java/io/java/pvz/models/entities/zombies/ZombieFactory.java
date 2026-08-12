package io.java.pvz.models.entities.zombies;

import io.java.pvz.models.Settings;
import io.java.pvz.models.entities.obstacle.ArcadeMachine;
import io.java.pvz.models.entities.obstacle.Barrel;
import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.entities.zombies.armour.ArmorData;
import io.java.pvz.models.entities.zombies.armour.ArmorLoader;
import io.java.pvz.models.entities.zombies.behavior.attack.*;
import io.java.pvz.models.entities.zombies.behavior.context.*;
import io.java.pvz.models.entities.zombies.behavior.defense.*;
import io.java.pvz.models.entities.zombies.behavior.effect.*;
import io.java.pvz.models.entities.zombies.behavior.move.*;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ZombieFactory {
    public static void init(String jsonPath) {
        ZombieLoader.init(jsonPath);
    }

    public static Zombie create(ZombieType type, int row) {
        ZombieData data = ZombieLoader.getInstance().get(type);
        Zombie zombie = new Zombie(type, data, row, null, null, null);

        assignBehaviors(zombie, type, data);

        applyInherentEffects(type, zombie);
        addStartingArmor(zombie, data);

        applyDifficultyModifiers(zombie);

        return zombie;
    }

    private static void assignBehaviors(Zombie zombie, ZombieType type, ZombieData data) {
        switch (type) {
            case PROSPECTOR -> {
                ProspectorContext ctx = new ProspectorContext();
                zombie.setMoveBehavior(new ProspectorMove(zombie, ctx));
                zombie.setDefenseBehavior(new ProspectorDefense( ctx));
                zombie.setAttackBehavior(new ProspectorAttack(zombie));
            }
            case EXPLORER -> {
                ExplorerContext ctx = new ExplorerContext();
                zombie.setMoveBehavior(new NormalMove(zombie));
                zombie.setAttackBehavior(new TorchBurnAttack(zombie, ctx));
                zombie.setDefenseBehavior(new ExplorerDefense(ctx));
            }
            case CRYSTAL_SKULL -> {
                TurquoiseContext ctx = new TurquoiseContext(zombie);
                zombie.setMoveBehavior(new TurquoiseMove(zombie, ctx));
                zombie.setAttackBehavior(new LaserAttack(zombie, ctx));
                zombie.setDefenseBehavior(new NormalDefense());
            }
            case ALL_STAR -> {
                AllStarContext ctx = new AllStarContext();
                zombie.setMoveBehavior(new AllStarMove(zombie, ctx));
                zombie.setAttackBehavior(new AllStarSmashAttack(zombie, ctx));
                zombie.setDefenseBehavior(new NormalDefense());
            }
            case JUGGLER -> {
                JugglerContext ctx = new JugglerContext();
                zombie.setMoveBehavior(new JugglerMove(zombie, ctx));
                zombie.setDefenseBehavior(new JugglerDefense(zombie, ctx));
                zombie.setAttackBehavior(new NormalAttack(zombie));
            }
            case SNORKEL -> {
                SnorkelContext ctx = new SnorkelContext();
                zombie.setMoveBehavior(new SnorkelMove(zombie, ctx));
                zombie.setAttackBehavior(new SnorkelAttack(zombie, ctx));
                zombie.setDefenseBehavior(new SnorkelDefense(ctx));
            }
            default -> {
                zombie.setMoveBehavior(getMoveAI(type, zombie));
                zombie.setAttackBehavior(getAttackAI(type, zombie, data));
                zombie.setDefenseBehavior(getDefenseAI(type, zombie));
            }
        }
    }

    private static MoveBehavior getMoveAI(ZombieType type, Zombie zombie) {
        return switch (type) {
            case GARGANTUAR -> new GargantuarMove(zombie);
            case NEWSPAPER -> new NewspaperMove(zombie);
            case BARREL_ROLLER -> createBarrelMove(zombie);
            case ARCADE -> createArcadeMove(zombie);
            case TROGLOBITE -> createTroglobiteMove(zombie);
            case DODO -> new DodoMove(zombie);

            case TOMB_RAISER -> new NormalMove(zombie);
            case HUNTER -> new NormalMove(zombie);
            case OCTOPUS -> new NormalMove(zombie);
            case WIZARD -> new NormalMove(zombie);
            case PIANIST -> createPianistMove(zombie);

            case FISHERMAN -> new StationaryMove(zombie);

            case KING -> new StationaryMove(zombie);

            case ZOMBOTANY_PEASHOOTER -> new PeriodicActionMove(zombie, 15, true,
                    () -> zombie.getAttackBehavior().execute());

            default -> new NormalMove(zombie);
        };
    }

    private static MoveBehavior createPianistMove(Zombie zombie) {
        return new PeriodicActionMove(zombie, 4 * TimeManager.TICKS_PER_SECOND, true, () -> {
            GameSession session = GameSession.getInstance();
            Random rand = new Random();
            int maxRows = session.getArena().getRows();

            for (Zombie z : session.getArena().getActiveZombies()) {
                if (z == zombie || z.isDead() || z.isHypnotized()) continue;

                List<Integer> possibleRows = new ArrayList<>();
                if (z.getRow() > 0) possibleRows.add(z.getRow() - 1);
                if (z.getRow() < maxRows - 1) possibleRows.add(z.getRow() + 1);

                if (!possibleRows.isEmpty()) {
                    z.setRow(possibleRows.get(rand.nextInt(possibleRows.size())));
                }
            }
        });
    }

    private static AttackBehavior getAttackAI(ZombieType type, Zombie zombie, ZombieData data) {
        Settings settings = Settings.getInstance();
        float dmgMultiplier = settings.getZombieDamageMultiplier();
        return switch (type) {
            case PIANIST, BARREL_ROLLER -> new SquashHit(zombie);
            case GARGANTUAR -> new SmashAttack(zombie, (int) (data.getSmashDamage() * dmgMultiplier));
            case KING -> new KingAttack(zombie);
            case DODO -> new DodoAttack(zombie);
            case ZOMBOTANY_SQUASH -> new SquashSuicideAttack(zombie);
            case ZOMBOTANY_PEASHOOTER -> new RangedAttack(zombie, ProjectileType.PEA, ProjectileType.NORMAL_PEA_DAMAGE);
            default -> new NormalAttack(zombie);
        };
    }

    private static DefenseBehavior getDefenseAI(ZombieType type, Zombie zombie) {
        return switch (type) {
            case JANE -> new ParasolDefense();
            case IMP_DRAGON -> new DragonImpDefense();
            default -> new NormalDefense();
        };
    }

    private static MoveBehavior createBarrelMove(Zombie zombie) {
        Barrel barrel = new Barrel(zombie.getCol(), zombie.getRow());
        if (GameSession.getInstance() != null) GameSession.getInstance().getArena().getActiveObstacles().add(barrel);
        return new PushMove(zombie,
                () -> !barrel.isDestroyed(),
                () -> {
                    float oldX = zombie.getX();
                    zombie.moveForward();
                    barrel.push(zombie.getX() - oldX);
                    zombie.getAttackBehavior().execute();
                },
                new NormalMove(zombie)
        );
    }

    private static MoveBehavior createArcadeMove(Zombie zombie) {
        ArcadeMachine arcadeMachine = new ArcadeMachine(zombie.getCol(), zombie.getRow());
        if (GameSession.getInstance() != null)
            GameSession.getInstance().getArena().getActiveObstacles().add(arcadeMachine);
        return new PushMove(zombie,
                () -> !arcadeMachine.isDestroyed(),
                () -> {
                    float oldX = zombie.getX();
                    zombie.moveForward();
                    arcadeMachine.push(zombie.getX() - oldX);
                    arcadeMachine.getPosition().setX(zombie.getX() - 40);

                    GameSession session = GameSession.getInstance();
                    for (Plant p : session.getArena()
                            .getTile(arcadeMachine.getRow(), arcadeMachine.getCol()).getPlants()) {
                        p.takeDamage(99999);
                    }
                    for (Zombie z : session.getArena().getActiveZombies()) {
                        if (z.isHypnotized() && z.getRow()
                                == arcadeMachine.getRow() && Math.abs(z.getX() - arcadeMachine.getX()) < 30) {
                            z.takeDamage(99999);
                        }
                    }
                },
                new NormalMove(zombie)
        );
    }

    private static MoveBehavior createTroglobiteMove(Zombie zombie) {
        IceBlock iceBlock = new IceBlock(zombie.getCol(), zombie.getRow());
        if (GameSession.getInstance() != null) GameSession.getInstance().getArena().getActiveObstacles().add(iceBlock);
        return new PushMove(zombie,
                () -> !iceBlock.isDestroyed(),
                () -> {
                    float oldX = zombie.getX();
                    zombie.moveForward();
                    iceBlock.push(zombie.getX() - oldX);
                    iceBlock.getPosition().setX(zombie.getX() - 40);

                    GameSession session = GameSession.getInstance();
                    for (Plant p : session.getArena().getTile(iceBlock.getRow(), iceBlock.getCol()).getPlants()) {
                        p.takeDamage(99999);
                    }
                    for (Zombie z : session.getArena().getActiveZombies()) {
                        if (z.isHypnotized() &&
                                z.getRow() == iceBlock.getRow() &&
                                Math.abs(z.getX() - iceBlock.getX()) < 30) {
                            z.takeDamage(99999);
                        }
                    }
                },
                new NormalMove(zombie)
        );
    }

    private static void applyInherentEffects(ZombieType type, Zombie zombie) { // effects that execute from birth
        if (Objects.requireNonNull(type) == ZombieType.RA) {
            zombie.addEffect(new SunAbsorber(zombie, 100, 50));
        }
        if (type == ZombieType.ZOMBOTANY_JALAPENO)
            zombie.addEffect(new JalapenoTimerEffect(zombie));

        if (type == ZombieType.TOMB_RAISER) {
            zombie.addEffect(new TombRaiserEffect(zombie, 5));
        }

        if (type == ZombieType.HUNTER) {
            zombie.addEffect(new HunterThrowEffect(zombie, 3));
        }

        if (type == ZombieType.FISHERMAN) {
            zombie.setCol(8);
            zombie.addEffect(new FishermanHookEffect(zombie, 4));
        }

        if (type == ZombieType.OCTOPUS) {
            zombie.addEffect(new OctopusTossEffect(zombie, 6));
        }

        if (type == ZombieType.WIZARD) {
            zombie.addEffect(new WizardSpellEffect(zombie, 6));
        }

        if (type == ZombieType.KING) {
            zombie.addEffect(new KingKnightEffect(zombie, 5));
        }

    }

    private static void applyDifficultyModifiers(Zombie zombie) {
        Settings settings = Settings.getInstance();

        int newHealth = (int) (zombie.getHealth() * settings.getZombieHealthMultiplier());
        zombie.setHealth(newHealth);
        zombie.setBaseHp(newHealth);

        int newCost = (int) (zombie.getWaveCost() * settings.getZombieCostMultiplier());
        zombie.setWaveCost(newCost);

        int newDamage = (int) (zombie.getEatDPS() * settings.getZombieDamageMultiplier());
        zombie.setEatDPS(newDamage);
    }

    private static void addStartingArmor(Zombie zombie, ZombieData data) {
        for (String armorProp : data.getArmorProps()) {
            ArmorData armorData = ArmorLoader.getInstance().get(armorProp);
            zombie.addArmor(new Armor(armorData));
        }
    }

    public static Zombie createTemplate(ZombieType type) {
        ZombieData data = ZombieLoader.getInstance().get(type);

        Zombie templateZombie = new Zombie(type, data, 0, null, null, null);

        addStartingArmor(templateZombie, data);
        applyDifficultyModifiers(templateZombie);

        return templateZombie;
    }
}
