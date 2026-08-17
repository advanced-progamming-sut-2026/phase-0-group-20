package io.java.pvz.models.game.adventure.levels.conditions;

import io.java.pvz.models.entities.zombies.zomboss.Zomboss;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.WinCondition;

public class BossWinCondition implements WinCondition {
    private final Zomboss boss;

    public BossWinCondition(Zomboss boss) {
        this.boss = boss;
    }

    @Override
    public boolean isWon(GameSession session) {
        return boss != null && boss.isDead();
    }
}
