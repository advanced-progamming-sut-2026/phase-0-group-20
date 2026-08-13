package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieFactory;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class Barrel extends PushableObstacle {

    public Barrel(int col, int row) {
        super(PushableObjectType.BARREL, col, row, 1200);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDestroyed) return;
        health -= damage;
        GameSession.notify("Barrel in (" + (position.getCol() + 1) + "," + (position.getRow() + 1) + ") took " + damage + " damage.");

        if (health <= 0) {
            health = 0;
            destroyBarrel();
        }
    }

    public void destroyBarrel() {
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
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        Zombie imp1 = ZombieFactory.create(ZombieType.IMP, this.getRow());
        imp1.setCol(this.getCol());
        imp1.setX(this.getX());
        session.getTimeManager().registerNewTicker(imp1);

        Zombie imp2 = ZombieFactory.create(ZombieType.IMP, this.getRow());
        imp2.setCol(this.getCol());
        imp2.setX(this.getX() + 35f);
        session.getTimeManager().registerNewTicker(imp2);

        session.getArena().addZombie(imp1);
        session.getArena().addZombie(imp2);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("BARREL_BREAK")
                .pixelCoordinate(this.getX(), this.getPosition().getY())
                .build());

        GameSession.notify("Barrel destroyed! Spawning 2 Imps...");
    }
}
