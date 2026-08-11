package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;

import java.util.List;

public class DodoAttack implements AttackBehavior {
    private final Zombie zombie;
    private final AttackBehavior normalEatAttack;

    public DodoAttack(Zombie zombie) {
        this.zombie = zombie;
        this.normalEatAttack = new NormalAttack(zombie);
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();

        List<Plant> targetPlants = session.getArena().getTile(zombie.getRow(), zombie.getCol()).getPlants();
        if (targetPlants.isEmpty()) {
            zombie.setAttacking(false);
            zombie.setState(ZombieState.WALKING);
            return;
        }

        normalEatAttack.execute();
    }
}
