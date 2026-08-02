package io.java.pvz.models.game.adventure.levels.conditions;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;

import java.util.List;

public class DeadLineLoseCondition implements LoseCondition {
    private final int loseCol;

    public DeadLineLoseCondition(int loseCol) {
        this.loseCol = loseCol;
    }

    @Override
    public boolean isLost(GameSession session) {
        List<Zombie> activeZombies = session.getArena().getActiveZombies();
        for (Zombie z : activeZombies) {
            if (z.getCol() <= loseCol && !z.isDead()) {
                notify("A zombie has passed the DeadLine. YOU LOST.");
                return true;
            }
        }
        return false;
    }
}
