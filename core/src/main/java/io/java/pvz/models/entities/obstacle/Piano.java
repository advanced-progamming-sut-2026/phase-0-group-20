package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class Piano extends PushableObstacle {
    private Zombie pianistZombie;

    public Piano(int col, int row) {
        super(PushableObjectType.PIANO, col, row, 300);
    }

    public void setPianistZombie(Zombie pianistZombie) {
        this.pianistZombie = pianistZombie;
    }

    @Override
    public void takeDamage(int damage) {
        if (isDestroyed) return;
        health -= damage;
        GameSession.notify("Piano took " + damage + " damage.");

        if (health <= 0) {
            health = 0;
            destroyPiano();
        }
    }

    public void destroyPiano() {
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
        GameSession.notify("Piano destroyed!");

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("PIANO_BREAK")
                .pixelCoordinate(this.getX(), this.getPosition().getY())
                .build());

        if (pianistZombie != null && !pianistZombie.isDead()) {
            pianistZombie.takeDamage(99999);
        }
    }

    public boolean isPlaying() {
        return pianistZombie != null && !pianistZombie.isDead() && pianistZombie.getState() == ZombieState.SPECIAL;
    }
}
