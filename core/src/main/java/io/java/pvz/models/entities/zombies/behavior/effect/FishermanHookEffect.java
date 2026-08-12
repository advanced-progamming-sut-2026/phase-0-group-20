package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class FishermanHookEffect extends Effect {
    private enum Phase { INTRO, IDLE, CAST, CAST_LOOP, REEL, TOSS }

    private final int hookIntervalTicks;
    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;

    private int introTicks;
    private int castTicks;
    private int castLoopTicks;
    private int reelTicks;
    private int tossTicks;

    private Plant targetPlant;

    public FishermanHookEffect(Zombie zombie, int hookIntervalSeconds) {
        super(zombie, -1);
        this.hookIntervalTicks = hookIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.INTRO;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tIntro = (anim != null && anim.hasClip("intro")) ? anim.getDuration("intro") : 1.6333f;
        float tCast = (anim != null && anim.hasClip("cast")) ? anim.getDuration("cast") : 1.2667f;
        float tReel = (anim != null && anim.hasClip("reel")) ? anim.getDuration("reel") : 1.4667f;
        float tToss = (anim != null && anim.hasClip("toss")) ? anim.getDuration("toss") : 2.4333f;

        this.introTicks = (int) (tIntro * TimeManager.TICKS_PER_SECOND);
        this.castTicks = (int) (tCast * TimeManager.TICKS_PER_SECOND);
        this.castLoopTicks = (int) (1.0f * TimeManager.TICKS_PER_SECOND);
        this.reelTicks = (int) (tReel * TimeManager.TICKS_PER_SECOND);
        this.tossTicks = (int) (tToss * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void onApply() {
        this.currentPhase = Phase.INTRO;
        this.phaseTicksCounter = 0;
        this.intervalTicksCounter = 0;
        this.targetPlant = null;

        zombie.setBaseSpeed(0f);
        zombie.setCurrentSpeed(0f);
        zombie.setState(ZombieState.INTRO);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        switch (currentPhase) {
            case INTRO:
                phaseTicksCounter++;
                if (phaseTicksCounter >= introTicks) {
                    resetToIdle();
                }
                break;

            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= hookIntervalTicks) {
                    targetPlant = findTargetPlant();
                    if (targetPlant != null) {
                        currentPhase = Phase.CAST;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.CAST);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case CAST:
                phaseTicksCounter++;
                if (phaseTicksCounter >= castTicks) {
                    currentPhase = Phase.CAST_LOOP;
                    zombie.setState(ZombieState.CAST_LOOP);
                    phaseTicksCounter = 0;
                }
                break;

            case CAST_LOOP:
                phaseTicksCounter++;
                if (phaseTicksCounter >= castLoopTicks) {
                    if (targetPlant != null && !targetPlant.isDead()) {
                        currentPhase = Phase.REEL;
                        zombie.setState(ZombieState.REEL);
                        phaseTicksCounter = 0;
                    } else {
                        resetToIdle();
                    }
                }
                break;

            case REEL:
                phaseTicksCounter++;
                if (phaseTicksCounter >= reelTicks) {
                    if (targetPlant != null && !targetPlant.isDead()) {
                        int pCol = targetPlant.getPlacedTile().getCol();
                        int zCol = zombie.getCol();

                        if (pCol >= zCol - 1) {
                            currentPhase = Phase.TOSS;
                            zombie.setState(ZombieState.TOSS);
                            phaseTicksCounter = 0;
                        } else {
                            pullPlant();
                            resetToIdle();
                        }
                    } else {
                        resetToIdle();
                    }
                }
                break;

            case TOSS:
                phaseTicksCounter++;
                if (phaseTicksCounter == (int) (tossTicks * 0.5f)) {
                    if (targetPlant != null && !targetPlant.isDead()) {
                        targetPlant.takeDamage(99999);
                        notify("Fisherman tossed " + targetPlant.getName() + " into water!");
                    }
                }

                if (phaseTicksCounter >= tossTicks) {
                    resetToIdle();
                }
                break;
        }
    }

    private void resetToIdle() {
        currentPhase = Phase.IDLE;
        phaseTicksCounter = 0;
        targetPlant = null;
        if (!zombie.isDead()) {
            zombie.setState(ZombieState.WALKING);
        }
    }

    private Plant findTargetPlant() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = zombie.getCol();
        Plant nearestPlant = null;
        int rightmostCol = -1;

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile().getRow() == zRow && p.getPlacedTile().getCol() < zCol) {
                if (p.getPlacedTile().getCol() > rightmostCol) {
                    rightmostCol = p.getPlacedTile().getCol();
                    nearestPlant = p;
                }
            }
        }
        return nearestPlant;
    }

    private void pullPlant() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int pCol = targetPlant.getPlacedTile().getCol();

        Tile oldTile = session.getArena().getTile(zRow, pCol);
        Tile newTile = session.getArena().getTile(zRow, pCol + 1);

        if (newTile.getPlants().isEmpty()) {
            oldTile.getPlants().remove(targetPlant);
            newTile.getPlants().add(targetPlant);
            targetPlant.setPlacedTile(newTile);
            targetPlant.setPosition(new Position(pCol + 1, zRow));

            notify("Fisherman pulled " + targetPlant.getName() + " to column " + (pCol + 1));
        } else {
            targetPlant.takeDamage(99999);
            notify("Fisherman destroyed " + targetPlant.getName() + " because path was blocked!");
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        resetToIdle();
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
