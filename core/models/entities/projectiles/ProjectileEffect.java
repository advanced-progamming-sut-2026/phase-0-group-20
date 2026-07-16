package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
