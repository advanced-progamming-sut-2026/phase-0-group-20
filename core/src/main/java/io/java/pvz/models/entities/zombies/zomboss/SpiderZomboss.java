package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpiderZomboss extends Zomboss {

    public SpiderZomboss(int row) {
        super(ZombieType.NORMAL, row, null, null, null);
    }

    @Override
    public void init() {
        this.setDefenseBehavior(new ZombossDefenseBehavior(this));

        List<ZombieType> allowedZombies = Arrays.asList(
            ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET,
            ZombieType.RA, ZombieType.EXPLORER, ZombieType.TOMB_RAISER,
            ZombieType.CRYSTAL_SKULL, ZombieType.GARGANTUAR, ZombieType.IMP
        );

        List<IZombossAttack> attacks = new ArrayList<>();
        IdleZombossAttack idleAttack = new IdleZombossAttack(this, attacks);

        attacks.add(new ZombossSpawnZombiesAttack(this, idleAttack, allowedZombies));
        attacks.add(new RobotDashAttack(this, idleAttack));

        MissileImpactBehavior egyptImpact = targetTile -> {
            DragonScorchedEarthAttack.burnTheTile(targetTile);
            GameSession.notify("Spider Zomboss missile exploded and cratered the tile!");
        };
        attacks.add(new ZombossMissileAttack(this, idleAttack, egyptImpact));

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();

        SwitchLaneZombossMove switchMove = new SwitchLaneZombossMove(this);
        IdleZombossMove idleMove = new IdleZombossMove(this, switchMove);
        switchMove.setIdleMove(idleMove);

        this.setMoveBehavior(idleMove);
        idleMove.onEnter();
    }
}
