package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieFactory;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.behavior.effect.GargantuarThrowImpEffect;
import io.java.pvz.models.game.GameSession;

public class GargantuarMove implements MoveBehavior {
    private final Zombie zombie;
    private boolean impThrown;

    public GargantuarMove(Zombie zombie) {
        this.zombie = zombie;
        this.impThrown = false;
    }

    @Override
    public void execute() {
        if (zombie.getState() == ZombieState.THROW_IMP || zombie.getState() == ZombieState.SMASH) return;

        if (!impThrown && (zombie.getHealth() < zombie.getBaseHp())) {
            impThrown = true;
            zombie.addEffect(new GargantuarThrowImpEffect(zombie));
            return;
        }
        zombie.moveForward();
    }
}
