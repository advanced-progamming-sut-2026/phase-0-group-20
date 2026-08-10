package io.java.pvz.models.fields.modifiers;

import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface SeasonModifier {

    void onCurrentLevelStart();

    void onWaveStart(Wave wave);

    void onZombieSpawn(Zombie zombie, Arena arena);

    void updateEnvironment(int currentTick, Arena arena);

    default void notify(String message) {
//        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
//                new GameEventPayload.Builder(GameEvent.NOTIFY)
//                        .message(message)
//                        .build());
    }

    default int getCurrentLevelNumber() {
        if (GameSession.getInstance().getCurrentMode() instanceof Level currentLevel)
            return currentLevel.getLevelNumber();

        return 1;
    }
}
