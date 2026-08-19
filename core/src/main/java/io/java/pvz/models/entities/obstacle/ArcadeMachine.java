package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class ArcadeMachine extends PushableObstacle {

    public ArcadeMachine(int col, int row) {
        super(PushableObjectType.ARCADE_MACHINE, col, row, 100);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDestroyed) return;
        health -= damage;
        if (health <= 0) {
            health = 0;
            destroyMachine();
        }
    }

    public void destroyMachine() {
        if (isDestroyed) return;
        isDestroyed = true;
        onDestroy();

        GameSession session = GameSession.getInstance();
        if (session != null && session.getArena() != null) {
            session.getArena().getActiveObstacles().remove(this);
        }
    }

    @Override
    public void onDestroy() {

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("ARCADE_MACHINE_BREAK")
                .pixelCoordinate(this.getX(), this.getPosition().getY())
                .build());
    }
}
