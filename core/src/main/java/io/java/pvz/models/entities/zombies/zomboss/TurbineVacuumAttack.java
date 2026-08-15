package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class TurbineVacuumAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;

    private int attackTimer;

    private static final int PHASE_1_START_TICKS = 2 * TimeManager.TICKS_PER_SECOND;
    private static final int PHASE_2_END_TICKS = 5 * TimeManager.TICKS_PER_SECOND;
    private static final int TOTAL_DURATION_TICKS = (int) (7.5 * TimeManager.TICKS_PER_SECOND);

    public TurbineVacuumAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;

        zomboss.setState(ZombieState.SPELL);
        dispatchTurbineEvent("TURBINE_START");
        zomboss.notify("Zomboss is powering up the Turbine Vacuum!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == PHASE_1_START_TICKS) {
            dispatchTurbineEvent("TURBINE_LOOP");
            zomboss.notify("Turbine Vacuum is active! Sucking everything in its path!");
        }

        else if (attackTimer > PHASE_1_START_TICKS && attackTimer <= PHASE_2_END_TICKS) {

            vacuumZombiesContinuous();

            if ((attackTimer - PHASE_1_START_TICKS) % 3 == 0) {
                vacuumPlantsForward();
            }
        }

        else if (attackTimer == PHASE_2_END_TICKS + 1) {
            dispatchTurbineEvent("TURBINE_END");
            zomboss.notify("Turbine Vacuum is powering down...");
        }

        else if (attackTimer >= TOTAL_DURATION_TICKS) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }


    private void vacuumZombiesContinuous() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        int[] targetRows = {zomboss.getRow(), zomboss.getSecondRow()};
        float pullSpeed = 5.0f;
        float zombossMouthX = zomboss.getX() - 20f;

        List<Zombie> activeZombies = new ArrayList<>(arena.getActiveZombies());

        for (Zombie z : activeZombies) {
            if (z instanceof Zomboss || z.isDead()) continue;

            boolean inTargetRow = false;
            for (int r : targetRows) {
                if (z.getRow() == r) {
                    inTargetRow = true;
                    break;
                }
            }

            if (inTargetRow) {
                float newX = z.getX() + pullSpeed;
                z.setX(newX);

                int newCol = (int) (newX / PhysicalConstants.TILE_UNIT_LENGTH);
                if (newCol != z.getCol()) {
                    z.setCol(newCol);
                }

                if (newX >= zombossMouthX) {
                    z.takeDamage(99999, null);
                }
            }
        }
    }


    private void vacuumPlantsForward() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        int zombossCol = zomboss.getCol();
        int[] targetRows = {zomboss.getRow(), zomboss.getSecondRow()};

        for (int row : targetRows) {
            if (row < 0 || row >= arena.getRows()) continue;

            for (int col = zombossCol - 1; col >= 0; col--) {
                Tile currentTile = arena.getTile(row, col);

                if (currentTile == null || currentTile.getPlants().isEmpty()) continue;

                List<Plant> plantsToMove = new ArrayList<>(currentTile.getPlants());

                for (Plant plant : plantsToMove) {
                    int nextCol = col + 1;

                    if (nextCol >= zombossCol) {
                        plant.takeDamage(99999);
                    } else {
                        Tile nextTile = arena.getTile(row, nextCol);
                        if (nextTile != null) {
                            currentTile.getPlants().remove(plant);
                            nextTile.addPlant(plant);
                        }
                    }
                }
            }
        }
    }

    private void dispatchTurbineEvent(String animationType) {
        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message(animationType)
                .build());
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }


}
