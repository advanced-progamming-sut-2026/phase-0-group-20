package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DragonZomboss extends Zomboss {

    public DragonZomboss(int row) {
        super(ZombieType.ZOMBOSS_DARK_AGES
            , row, null, null, null);
    }

    @Override
    public void init() {
        this.setDefenseBehavior(new ZombossDefenseBehavior(this));

        List<ZombieType> allowedZombies = Arrays.asList(
            ZombieType.NORMAL, ZombieType.CONE, ZombieType.DARK_ARMOR,
            ZombieType.JUGGLER, ZombieType.WIZARD, ZombieType.KING,
            ZombieType.PIANIST, ZombieType.ARCADE, ZombieType.GARGANTUAR,
            ZombieType.IMP_DRAGON
        );

        List<IZombossAttack> attacks = new ArrayList<>();
        IdleZombossAttack idleAttack = new IdleZombossAttack(this, attacks);

//        attacks.add(new ZombossSpawnZombiesAttack(this, idleAttack, allowedZombies));
//        attacks.add(new DragonFireballAttack(this, idleAttack));
        attacks.add(new DragonScorchedEarthAttack(this, idleAttack));

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();

        SwitchLaneZombossMove switchMove = new SwitchLaneZombossMove(this);
        IdleZombossMove idleMove = new IdleZombossMove(this, switchMove);
        switchMove.setIdleMove(idleMove);

        this.setMoveBehavior(idleMove);
        idleMove.onEnter();
    }
}
