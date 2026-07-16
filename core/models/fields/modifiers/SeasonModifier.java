package models.fields.modifiers;

import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;
import models.game.adventure.levels.Level;

public interface SeasonModifier {
    void onWaveStart(Wave wave);

    void onZombieSpawn(Zombie zombie, Arena arena);

    void updateEnvironment(int currentTick, Arena arena);

    default int getCurrentLevelNumber() {
        if (GameSession.getInstance().getCurrentMode() instanceof Level currentLevel)
            return currentLevel.getLevelNumber();

        return 1;
    }
}
