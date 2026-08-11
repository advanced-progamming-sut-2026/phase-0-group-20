package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.GraveStoneTile;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TombRaiserEffect extends Effect {
    private enum Phase { IDLE, POWER }

    private final int throwIntervalTicks;
    private final Random random = new Random();

    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;
    private int powerTicks;
    private int throwBoneTick;
    private boolean boneThrown;

    public TombRaiserEffect(Zombie zombie, int throwIntervalSeconds) {
        super(zombie, -1);
        this.throwIntervalTicks = throwIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.IDLE;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float pDur = (anim != null && anim.hasClip("power")) ? anim.getDuration("power") : 3.0f;
        this.powerTicks = (int) (pDur * TimeManager.TICKS_PER_SECOND);

        this.throwBoneTick = (int) (powerTicks * 0.6f);
    }

    @Override
    public void onApply() {
        this.currentPhase = Phase.IDLE;
        this.intervalTicksCounter = 0;
        this.phaseTicksCounter = 0;
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        switch (currentPhase) {
            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= throwIntervalTicks) {
                    if (hasEmptyTileForGrave()) {
                        currentPhase = Phase.POWER;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        boneThrown = false;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.POWER);
                        zombie.applySpeedMultiplier(0f);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case POWER:
                phaseTicksCounter++;

                if (phaseTicksCounter == throwBoneTick && !boneThrown) {
                    throwBone();
                    boneThrown = true;
                }

                if (phaseTicksCounter >= powerTicks) {
                    currentPhase = Phase.IDLE;
                    phaseTicksCounter = 0;

                    if (!zombie.isDead()) {
                        zombie.setState(ZombieState.WALKING);
                        zombie.resetSpeed();
                    }
                }
                break;
        }
    }

    private boolean hasEmptyTileForGrave() {
        GameSession session = GameSession.getInstance();
        for (int r = 0; r < session.getArena().getRows(); r++) {
            for (int c = 5; c < session.getArena().getCols(); c++) {
                Tile tile = session.getArena().getTile(r, c);
                if (tile.getPlants().isEmpty() && !(tile instanceof GraveStoneTile)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void throwBone() {
        GameSession session = GameSession.getInstance();
        List<Tile> emptyTiles = new ArrayList<>();

        for (int r = 0; r < session.getArena().getRows(); r++) {
            for (int c = 5; c < session.getArena().getCols(); c++) {
                Tile tile = session.getArena().getTile(r, c);
                if (tile.getPlants().isEmpty() && !(tile instanceof GraveStoneTile)) {
                    emptyTiles.add(tile);
                }
            }
        }

        if (!emptyTiles.isEmpty()) {
            Tile targetTile = emptyTiles.get(random.nextInt(emptyTiles.size()));

            session.getArena().changeTile(
                targetTile.getRow(),
                targetTile.getCol(),
                new GraveStoneTile(targetTile.getRow(), targetTile.getCol())
            );

            GameSession.notify("Grave spawned at: " + (targetTile.getCol() + 1) + ", " + (targetTile.getRow() + 1));

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("BONE_HIT")
                    .coordinate(targetTile.getRow(), targetTile.getCol())
                    .build());
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!zombie.isDead()) {
            zombie.resetSpeed();
            zombie.setState(ZombieState.WALKING);
        }
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
