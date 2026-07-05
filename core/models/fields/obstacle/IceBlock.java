package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

public class IceBlock {
    public static final int MELT_RATE_PER_TICK = 6;
    private int iceHp = 600;

    private Plant frozenPlant;
    private Zombie frozenZombie;

    public IceBlock(Plant frozenPlant) {
        this.frozenPlant = frozenPlant;
    }

    public IceBlock(Zombie frozenZombie) {
        this.frozenZombie = frozenZombie;
    }

    public void takeDamage(int amount, int row, int col) {
        iceHp -= amount;
        if (iceHp <= 0) melt(row, col);
    }

    public void melt(int row, int col) {
        GameSession session = GameSession.getInstance();

        // آزاد کردن گیاه
        if (frozenPlant != null) {
            session.getArena().getTile(row, col).addPlant(frozenPlant);
            session.getArena().addPlant(frozenPlant);
            session.getTimeManager().registerNewTicker(frozenPlant);
            frozenPlant = null;
        }

        // آزاد کردن زامبی
        if (frozenZombie != null) {
            frozenZombie.setRow(row);
            frozenZombie.setX(col);
            session.getArena().addZombie(frozenZombie);
            session.getTimeManager().registerNewTicker(frozenZombie);
            frozenZombie = null;
        }
        System.out.println("IceBlock melted at [" + row + "][" + col + "]!");
    }

    public boolean hasFrozenPlant() { return frozenPlant != null; }
    public boolean hasFrozenZombie() { return frozenZombie != null; }
    // گترها و سترهای لازم...
}