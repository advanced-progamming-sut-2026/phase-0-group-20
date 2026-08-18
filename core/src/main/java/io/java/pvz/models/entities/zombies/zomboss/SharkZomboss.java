package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharkZomboss extends Zomboss {

    public SharkZomboss(int row) {
        super(ZombieType.ZOMBOSS_BEACH
            , row, null, null, null);
    }

    @Override
    public void init() {
        this.setDefenseBehavior(new ZombossDefenseBehavior(this));

        List<ZombieType> allowedZombies = Arrays.asList(
            ZombieType.NORMAL, ZombieType.CONE, ZombieType.BRICK,
            ZombieType.SNORKEL, ZombieType.OCTOPUS, ZombieType.FISHERMAN,
            ZombieType.NEWSPAPER, ZombieType.JANE, ZombieType.ALL_STAR,
            ZombieType.GARGANTUAR, ZombieType.IMP
        );

        List<IZombossAttack> attacks = new ArrayList<>();
        IdleZombossAttack idleAttack = new IdleZombossAttack(this, attacks);

        attacks.add(new ZombossSpawnZombiesAttack(this, idleAttack, allowedZombies));
        attacks.add(new SharkBiteAttack(this, idleAttack));
        attacks.add(new TurbineVacuumAttack(this, idleAttack));
        attacks.add(new SwitchLaneZombossAttack(this, idleAttack));

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();

        this.setMoveBehavior(null);
    }
}
