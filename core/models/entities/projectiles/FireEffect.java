package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

public class FireEffect implements ProjectileEffect {


    @Override
    public void applyEffect(GameSession gameSession) {

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