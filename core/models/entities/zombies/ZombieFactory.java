package models.entities.zombies;

import models.entities.zombies.armour.Armor;
import models.entities.zombies.armour.ArmorData;
import models.entities.zombies.armour.ArmorLoader;
import models.entities.zombies.behavior.attack.*;
import models.entities.zombies.behavior.defense.*;
import models.entities.zombies.behavior.effect.SunAbsorber;
import models.entities.zombies.behavior.move.*;

import java.util.Objects;

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
            case ARCADE, TROGLOBITE, BARREL_ROLLER -> new PushMove(zombie);
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
                // logic for this
            });
            case PIANIST -> new PeriodicActionMove(zombie, 4 * 10, true, () -> {
                // logic for dance
            });
            case FISHERMAN -> new PeriodicActionMove(zombie, 4 * 10, false, () -> {
                // logic for this
            });
            case KING -> new PeriodicActionMove(zombie, 3 * 10, false, () -> {
                // logic for add darkArmor
            }); // Parasol Zombie
            default -> new NormalMove(zombie);
        };
    }

    private static AttackBehavior getAttackAI(ZombieType type, Zombie zombie, ZombieData data) {
        return switch (type) {
            case ARCADE, BARREL_ROLLER, PIANIST -> new SquashHit(zombie);
            case GARGANTUAR, ALL_STAR -> new SmashAttack(zombie, data.getSmashDamage());
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
            default -> new NormalDefense();
        };
    }

    private static void applyInherentEffects(ZombieType type, Zombie zombie) { // effects that execute from birth
        if (Objects.requireNonNull(type) == ZombieType.RA) {
            zombie.addEffect(new SunAbsorber(zombie, 10, 50));
        }
    }
}
