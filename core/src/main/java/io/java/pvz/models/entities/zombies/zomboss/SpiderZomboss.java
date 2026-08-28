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

        List<ZombieType> allowedZombies = createAllowedZombiesList();
        List<IZombossAttack> attacks = new ArrayList<>();
        IdleZombossAttack idleAttack = new IdleZombossAttack(this, attacks);

        setupAttacks(attacks, idleAttack, allowedZombies);

        this.setAttackBehavior(idleAttack);
        idleAttack.onEnter();
        this.setMoveBehavior(null);
    }

    private List<ZombieType> createAllowedZombiesList() {
        return Arrays.asList(
            ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET,
            ZombieType.RA, ZombieType.EXPLORER, ZombieType.TOMB_RAISER,
            ZombieType.CRYSTAL_SKULL, ZombieType.GARGANTUAR, ZombieType.IMP
        );
    }

    private void setupAttacks(
        List<IZombossAttack> attacks, IdleZombossAttack idleAttack, List<ZombieType> allowedZombies
    ) {
        attacks.add(new ZombossSpawnZombiesAttack(this, idleAttack, allowedZombies));
        attacks.add(new RobotDashAttack(this, idleAttack));
        attacks.add(new ZombossMissileAttack(this, idleAttack, createEgyptImpactBehavior()));
        attacks.add(new SwitchLaneZombossAttack(this, idleAttack));
    }

    private MissileImpactBehavior createEgyptImpactBehavior() {
        return targetTile -> {
            dispatchMissileExplosion(targetTile);

            Arena arena = GameSession.getInstance().getArena();
            Random random = new Random();
            int gravesPlanted = 0;
            int attempts = 0;

            while (gravesPlanted < 2 && attempts < 20) {
                if (generateGrave(arena, random)) {
                    gravesPlanted++;
                }
                attempts++;
            }
        };
    }

    private void dispatchMissileExplosion(Tile targetTile) {
        GameEventMessenger.getInstance().dispatch(
            GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("MISSILE_EXPLOSION")
                .coordinate(targetTile.getRow(), targetTile.getCol())
                .build()
        );
    }

    private boolean generateGrave(Arena arena, Random random) {
        int r = random.nextInt(arena.getRows());
        int c = 2 + random.nextInt(arena.getCols() - 2);
        Tile rndTile = arena.getTile(r, c);

        if (!(rndTile instanceof GraveStoneTile)) {
            arena.changeTile(
                rndTile.getRow(),
                rndTile.getCol(),
                new GraveStoneTile(rndTile.getRow(), rndTile.getCol())
            );
            dispatchBoneHit(r, c);
            return true;
        }

        return false;
    }

    private void dispatchBoneHit(int r, int c) {
        GameEventMessenger.getInstance().dispatch(
            GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("BONE_HIT")
                .coordinate(r, c)
                .build()
        );
    }
}
