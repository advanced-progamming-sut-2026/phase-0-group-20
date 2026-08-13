package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.obstacle.ArcadeMachine;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;

public class ArcadeMove implements MoveBehavior, GameEventListener {
    private final Zombie zombie;
    private final ArcadeMachine arcadeMachine;

    public ArcadeMove(Zombie zombie, ArcadeMachine arcadeMachine) {
        this.zombie = zombie;
        this.arcadeMachine = arcadeMachine;

        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED, this);
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.ZOMBIE_KILLED && payload != null && payload.getZombie() == this.zombie) {
            destroyMachineIfExists();
            GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED, this);
        }
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        if (zombie.isDead()) {
            destroyMachineIfExists();
            return;
        }

        if (arcadeMachine != null && !arcadeMachine.isDestroyed()) {
            zombie.setAttacking(false);
            zombie.setState(ZombieState.PUSH);

            float oldX = zombie.getX();
            zombie.moveForward();
            float dx = zombie.getX() - oldX;

            arcadeMachine.push(dx);
            arcadeMachine.getPosition().setX(zombie.getX() - PhysicalConstants.TILE_WIDTH);

            crushEntitiesInFrontOfArcade(arcadeMachine);
        } else {
            if (zombie.getState() == ZombieState.PUSH) {
                zombie.setState(ZombieState.WALKING);
            }
            zombie.moveForward();
        }
    }

    private void destroyMachineIfExists() {
        if (arcadeMachine != null && !arcadeMachine.isDestroyed()) {
            arcadeMachine.destroyMachine();
        }
    }

    private void crushEntitiesInFrontOfArcade(ArcadeMachine machine) {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<Plant> plantsToCrush = new ArrayList<>();
        Tile tile = session.getArena().getTile(machine.getRow(), machine.getCol());
        if (tile != null && !tile.getPlants().isEmpty()) {
            plantsToCrush.addAll(tile.getPlants());
        }

        for (Plant plant : session.getArena().getActivePlants()) {
            if (plant != null && !plant.isDead() && plant.getPlacedTile() != null) {
                if (plant.getPlacedTile().getRow() == machine.getRow()) {
                    float dist = Math.abs(plant.getPosition().getX() - machine.getX());
                    if (dist <= PhysicalConstants.TILE_WIDTH * 0.75f) {
                        if (!plantsToCrush.contains(plant)) {
                            plantsToCrush.add(plant);
                        }
                    }
                }
            }
        }

        for (Plant plant : plantsToCrush) {
            if (plant != null && !plant.isDead()) {
                plant.takeDamage(99999);
                if (plant.getPlacedTile() != null) {
                    plant.getPlacedTile().getPlants().remove(plant);
                }
                session.getArena().getActivePlants().remove(plant);
                session.getTimeManager().unregisterTicker(plant);
                GameSession.notify("Arcade Machine crushed " + plant.getName() + "!");
            }
        }

        List<Zombie> zombiesToCrush = new ArrayList<>();
        for (Zombie z : session.getArena().getActiveZombies()) {
            if (z != null && z.isHypnotized() && !z.isDead() && z.getRow() == machine.getRow()) {
                float dist = Math.abs(z.getX() - machine.getX());
                if (dist <= PhysicalConstants.TILE_WIDTH * 0.75f) {
                    zombiesToCrush.add(z);
                }
            }
        }

        for (Zombie z : zombiesToCrush) {
            if (z != null && !z.isDead()) {
                z.takeDamage(99999);
                GameSession.notify("Arcade Machine crushed a hypnotized zombie!");
            }
        }
    }

    public ArcadeMachine getArcadeMachine() {
        return arcadeMachine;
    }
}
