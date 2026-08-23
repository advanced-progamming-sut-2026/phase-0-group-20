package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.obstacle.GraveHolder;
import io.java.pvz.models.entities.obstacle.GraveStone; // اگر اسم کلاس قبر چیز دیگری است این را تغییر بده
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.fields.tiles.GraveStoneTile;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SpiderZomboss extends Zomboss {

    public SpiderZomboss(int row) {
        super(ZombieType.ZOMBOSS_EGYPT, row, null, null, null);
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
            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("MISSILE_EXPLOSION")
                    .coordinate(targetTile.getRow(), targetTile.getCol())
                    .build());

            Arena arena = GameSession.getInstance().getArena();
            Random random = new Random();
            int gravesPlanted = 0;
            int attempts = 0;

            while (gravesPlanted < 2 && attempts < 20) {
                int r = random.nextInt(arena.getRows());
                int c = 2 + random.nextInt(arena.getCols() - 2);

                Tile rndTile = arena.getTile(r, c);

                if (!(rndTile instanceof GraveStoneTile)) {

                    GameSession.getInstance().getArena().changeTile(
                        targetTile.getRow(),
                        targetTile.getCol(),
                        new GraveStoneTile(targetTile.getRow(), targetTile.getCol())
                    );
                    gravesPlanted++;

                    GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                        new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                            .message("BONE_HIT")
                            .coordinate(r, c)
                            .build());
                }
                attempts++;
            }
        };

        attacks.add(new ZombossMissileAttack(this, idleAttack, egyptImpact));
        attacks.add(new SwitchLaneZombossAttack(this, idleAttack));

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();

        this.setMoveBehavior(null);
    }
}
