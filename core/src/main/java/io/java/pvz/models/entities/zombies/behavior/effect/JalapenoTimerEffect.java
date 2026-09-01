package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

public class JalapenoTimerEffect extends Effect {

    private static final int EXPLOSION_DELAY_TICKS = 10 * TimeManager.TICKS_PER_SECOND;

    public JalapenoTimerEffect(Zombie zombie) {
        super(zombie, EXPLOSION_DELAY_TICKS);
    }

    @Override
    public void onApply() {

    }

    @Override
    public void onRemove() {
        if (zombie == null || zombie.isDead()) return;

        GameSession session = GameSession.getInstance();
        int row = zombie.getRow();

        GameEventPayload firePayload = new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
            .message("JALAPENO_EXPLODE")
            .coordinate(row, zombie.getCol())
            .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT, firePayload);

        GameEventPayload explodePayload = new GameEventPayload.Builder(GameEvent.PLANT_EXPLODED).build();
        GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_EXPLODED, explodePayload);

        for (Plant p : session.getArena().getActivePlants())
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == row)
                p.takeDamage(Integer.MAX_VALUE);

        zombie.takeDamage(Integer.MAX_VALUE);
    }
}
