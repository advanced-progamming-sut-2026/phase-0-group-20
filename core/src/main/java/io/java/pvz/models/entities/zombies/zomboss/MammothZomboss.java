package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MammothZomboss extends Zomboss {

    public MammothZomboss(int row) {
        super(ZombieType.ZOMBOSS_FROZEN_CAVES
            , row, null, null, null);
    }

    @Override
    public void init() {
        this.setDefenseBehavior(new ZombossDefenseBehavior(this));

        List<ZombieType> allowedZombies = Arrays.asList(
            ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET,
            ZombieType.DODO, ZombieType.HUNTER, ZombieType.TROGLOBITE,
            ZombieType.PROSPECTOR, ZombieType.BARREL_ROLLER,
            ZombieType.GARGANTUAR, ZombieType.IMP
        );

        List<IZombossAttack> attacks = new ArrayList<>();
        IdleZombossAttack idleAttack = new IdleZombossAttack(this, attacks);

        attacks.add(new MammothFreezingWind(this, idleAttack));
        attacks.add(new MammothFreezingColumn(this, idleAttack, allowedZombies));

        MissileImpactBehavior iceImpact = targetTile -> {
            if (targetTile != null) {
                for (Plant p : new ArrayList<>(targetTile.getPlants())) {
                    p.takeDamage(150);
                }
            }
            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("MISSILE_EXPLOSION")
                    .coordinate(targetTile.getRow(), targetTile.getCol())
                    .build());
        };
        attacks.add(new ZombossMissileAttack(this, idleAttack, iceImpact));

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();

        this.setMoveBehavior(null);
    }
}
