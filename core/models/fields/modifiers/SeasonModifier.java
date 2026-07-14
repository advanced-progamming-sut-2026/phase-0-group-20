package models.fields.modifiers;

import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.game.Arena;

public interface SeasonModifier {
    void onWaveStart(Wave wave);

    void onZombieSpawn(Zombie zombie, Arena arena);

    void updateEnvironment(int currentTick, Arena arena);
}
