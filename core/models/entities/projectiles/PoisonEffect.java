package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public class PoisonEffect implements ProjectileEffect {

    public void applyEffect(Zombie zombie, Arena board) {
    }

    public int getDamageMultiplier() {
        return 0;
    }

    public boolean ignoresArmor() {
        return false;
    }

    public boolean meltsIce() {
        return false;
    }
}
