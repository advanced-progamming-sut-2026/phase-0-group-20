package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public class IceEffect implements ProjectileEffect {

    @Override
    public void applyEffect(Zombie zombie, Arena board) {
    }

    @Override
    public int getDamageMultiplier() {
        return 0;
    }

    @Override
    public boolean ignoresArmor() {
        return false;
    }

    @Override
    public boolean meltsIce() {
        return false;
    }
}
