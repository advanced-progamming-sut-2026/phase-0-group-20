package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface ProjectileEffect {

    void applyEffect(Zombie zombie, Projectile projectile);

    int getDamageMultiplier();

    boolean ignoresArmor();

    boolean meltsIce();

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }
}
