package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;

public class HypnotizeAttack implements AttackBehavior {
    private final Zombie zombie;

    public HypnotizeAttack(Zombie zombie) {
        this.zombie = zombie;
    }


    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Zombie targetZombie = zombie.getTargetZombie();

        if (targetZombie != null) {
            int damage = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;
            targetZombie.takeDamage(damage * 2);
            GameSession.notify("Hypnotized Zombie at (" + (zombie.getCol() + 1) + ", " + (zombie.getRow() + 1) +
                    ") attacked another zombie for " + damage + " damage!");
        }
    }
}
