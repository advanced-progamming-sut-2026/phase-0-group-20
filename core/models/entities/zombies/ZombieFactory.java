package models.entities.zombies;

import models.entities.plants.Plant;
import models.entities.plants.effect.CatEffect;
import models.entities.plants.effect.PlantEffect;
import models.entities.zombies.armour.Armor;
import models.entities.zombies.armour.ArmorData;
import models.entities.zombies.armour.ArmorLoader;
import models.entities.zombies.behavior.attack.*;
import models.entities.zombies.behavior.defense.*;
import models.entities.zombies.behavior.effect.SunAbsorber;
import models.entities.zombies.behavior.move.*;
import models.game.GameSession;

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

        zombie.setMoveBehavior(getMoveAI(type, zombie));
        zombie.setAttackBehavior(getAttackAI(type, zombie, data));
        zombie.setDefenseBehavior(getDefenseAI(type, zombie));

        applyInherentEffects(type, zombie);
        addStartingArmor(zombie, data);

        return zombie;
    }

    private static void addStartingArmor(Zombie zombie, ZombieData data) {
        for (String armorProp : data.getArmorProps()) {
            ArmorData armorData = ArmorLoader.getInstance().get(armorProp);
            zombie.addArmor(new Armor(armorData));
        }
    }

    private static MoveBehavior getMoveAI(ZombieType type, Zombie zombie) {
        return switch (type) {
            case GARGANTUAR -> new GargantuarMove(zombie);
            case ALL_STAR -> new AllStarMove(zombie);
            case PROSPECTOR -> new ProspectorMove(zombie);
            case CRYSTAL_SKULL -> new TurquoiseMove(zombie);
//            case ARCADE, TROGLOBITE, BARREL_ROLLER -> new PushMove(zombie);
            case ARCADE, TROGLOBITE -> new PushMove(zombie);
            case NEWSPAPER -> new NewspaperMove(zombie);
            case RA -> new RaMove(zombie);
            case DODO -> new DodoMove(zombie);
            case SNORKEL -> new SnorkelMove(zombie);
            case JUGGLER -> new JugglerMove(zombie);
            case TOMB_RAISER -> new PeriodicActionMove(zombie, 5 * 10, true, () -> {
                // logic for new grave
            });
            case HUNTER -> new PeriodicActionMove(zombie, 3 * 10, true, () -> {
                // logic for shoot ice ball
            });
            case OCTOPUS -> new PeriodicActionMove(zombie, 6 * 10, true, () -> {
                // logic for shoot octopus
            });
            case WIZARD -> new PeriodicActionMove(zombie, 5 * 10, true, () -> {
                GameSession session = GameSession.getInstance();
                List<Plant> activePlants = session.getArena().getActivePlants();

                List<Plant> validTargets = new ArrayList<>();
                for (Plant p : activePlants) {
                    boolean isCat = false;
                    for (PlantEffect effect : p.getActiveEffects()) {
                        if (effect instanceof CatEffect) {
                            isCat = true;
                            break;
                        }
                    }
                    if (!isCat) validTargets.add(p);
                }

                if (!validTargets.isEmpty()) {
                    Plant target = validTargets.get(new Random().nextInt(validTargets.size()));
                    target.addEffect(new CatEffect(zombie));
                }
            });
            case PIANIST -> new PeriodicActionMove(zombie, 4 * 10, true, () -> {
                // logic for dance
            });
            case FISHERMAN -> new PeriodicActionMove(zombie, 4 * 10, false, () -> {
                // logic for this
            });
            case KING -> new PeriodicActionMove(zombie, 3 * 10, false,
                    () -> zombie.getAttackBehavior().execute());
            default -> new NormalMove(zombie);
        };
    }

    private static AttackBehavior getAttackAI(ZombieType type, Zombie zombie, ZombieData data) {
        return switch (type) {
//            case ARCADE, BARREL_ROLLER, PIANIST -> new SquashHit(zombie);
            case ARCADE, PIANIST -> new SquashHit(zombie);

            case GARGANTUAR -> new SmashAttack(zombie, data.getSmashDamage());
            case ALL_STAR -> new AllStarSmashAttack(zombie);
            case EXPLORER -> new TorchBurnAttack(zombie);
            case HUNTER -> new HunterFreezeAttack(zombie);
            case OCTOPUS -> new OctopusAttack(zombie);
            case CRYSTAL_SKULL -> new LaserAttack(zombie, 4000);
            case TOMB_RAISER -> new GraveSpawnAttack(zombie);
            case KING -> new KingAttack(zombie);
            case WIZARD -> new WizardTransformAttack(zombie);
            case FISHERMAN -> new FishermanHookAttack(zombie);
            default -> new NormalAttack(zombie);
        };
    }

    private static DefenseBehavior getDefenseAI(ZombieType type, Zombie zombie) {
        return switch (type) {
            case JANE -> new ParasolDefense();
            case JUGGLER -> new JugglerDefense(zombie);
            case IMP_DRAGON -> new DragonImpDefense();
            case SNORKEL -> new SnorkelDefense(zombie);
            default -> new NormalDefense();
        };
    }

    private static void applyInherentEffects(ZombieType type, Zombie zombie) { // effects that execute from birth
        if (Objects.requireNonNull(type) == ZombieType.RA) {
            zombie.addEffect(new SunAbsorber(zombie, 10, 50));
        }
    }
}
