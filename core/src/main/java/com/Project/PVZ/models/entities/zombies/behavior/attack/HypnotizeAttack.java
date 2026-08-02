package com.Project.PVZ.models.entities.zombies.behavior.attack;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.timeManager.TimeManager;

import java.util.List;

public class HypnotizeAttack implements AttackBehavior {
    private final Zombie zombie;

    public HypnotizeAttack(Zombie zombie) {
        this.zombie = zombie;
    }


    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Zombie targetZombie = findTargetZombie();

        if (targetZombie != null) {
            int damage = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;
            targetZombie.takeDamage(damage);
            zombie.notify("Hypnotized Zombie at (" + zombie.getCol() + ", " + zombie.getRow() +
                    ") attacked another zombie for " + damage + " damage!");
        }
    }

    private Zombie findTargetZombie() {
        Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());

        if (currentTile != null) {
            List<Zombie> zombiesInTile = GameSession.getInstance().getArena().getZombiesOnTile(currentTile);
            for (Zombie otherZombie : zombiesInTile) {
                if (otherZombie != this.zombie && !otherZombie.isDead() && !otherZombie.isHypnotized()) {
                    return otherZombie;
                }
            }
        }
        return null;
    }
}
