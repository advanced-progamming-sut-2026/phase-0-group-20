package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;

public class AreaOfEffect implements ProjectileEffect {
    private ProjectileEffect inner;

    public AreaOfEffect(ProjectileEffect inner) {
        this.inner = inner;
    }

    public void applyEffect(Zombie z, Arena board) {
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