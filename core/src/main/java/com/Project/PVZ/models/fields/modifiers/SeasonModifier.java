package com.Project.PVZ.models.fields.modifiers;

import com.Project.PVZ.models.entities.zombies.Wave;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.game.Arena;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.adventure.levels.Level;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

public interface SeasonModifier {

    void onCurrentLevelStart();

    void onWaveStart(Wave wave);

    void onZombieSpawn(Zombie zombie, Arena arena);

    void updateEnvironment(int currentTick, Arena arena);

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }

    default int getCurrentLevelNumber() {
        if (GameSession.getInstance().getCurrentMode() instanceof Level currentLevel)
            return currentLevel.getLevelNumber();

        return 1;
    }
}
