package models.entities.projectiles;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

public class IceEffect implements ProjectileEffect {

    @Override
    public void applyEffect(GameSession gameSession) {
        // slow down zombie


        System.out.println("A zombie was chilled by Ice Pea!");
    }

    @Override
    public int getDamageMultiplier() {
        return 1;
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
