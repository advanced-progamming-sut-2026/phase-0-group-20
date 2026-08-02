package com.Project.PVZ.models.entities.projectiles;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

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
