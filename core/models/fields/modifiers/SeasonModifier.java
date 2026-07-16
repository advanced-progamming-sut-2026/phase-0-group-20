package models.fields.modifiers;

import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.game.Arena;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.game.GameSession;
import models.game.adventure.levels.Level;

public interface SeasonModifier {
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
