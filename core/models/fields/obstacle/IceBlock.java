package models.fields.obstacle;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantTag;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.timeManager.Ticker;

public class IceBlock implements Ticker {
    public static final int MELT_RATE_PER_TICK = 6;
    private int iceHp = 600;
    private int row;
    private int col;

    private Plant frozenPlant;
    private Zombie frozenZombie;

    public IceBlock(Plant frozenPlant, int row, int col) {
        this.frozenPlant = frozenPlant;
        this.row = row;
        this.col = col;
    }

    public IceBlock(Zombie frozenZombie, int row, int col) {
        this.frozenZombie = frozenZombie;
        this.row = row;
        this.col = col;
    }

    @Override
    public void onTick(int currentTick) {
        if (hasAdjacentFirePlant()) takeDamage(MELT_RATE_PER_TICK);
    }

    private boolean hasAdjacentFirePlant() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return false;

        for (Plant plant : session.getArena().getActivePlants()) {
            if (plant.getCurrentHp() <= 0 || plant.getPlacedTile() == null) continue;
            if (!plant.getTags().contains(PlantTag.FIRE)) continue;

            int dRow = Math.abs(plant.getPlacedTile().getRow() - row);
            int dCol = Math.abs(plant.getPlacedTile().getCol() - col);
            if (dRow <= 1 && dCol <= 1 && (dRow != 0 || dCol != 0)) return true;
        }
        return false;
    }

    public void takeDamage(int amount) {
        iceHp -= amount;
        if (iceHp <= 0) melt();
    }

    public void melt() {
        GameSession session = GameSession.getInstance();
        Tile thisTile = session.getArena().getTile(row, col);

        if (frozenPlant != null) {
            thisTile.addPlant(frozenPlant);
            session.getArena().addPlant(frozenPlant);
            frozenPlant.setPlacedTile(thisTile);
            session.getTimeManager().registerNewTicker(frozenPlant);
            frozenPlant = null;
        }

        if (frozenZombie != null) {
            frozenZombie.setRow(row);
            frozenZombie.setCol(col);
            session.getArena().addZombie(frozenZombie);
            session.getTimeManager().registerNewTicker(frozenZombie);
            frozenZombie = null;
        }

        session.getTimeManager().unregisterTicker(this);
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("IceBlock melted at [" + row + "][" + col + "]!")
                        .build());
    }

    public boolean hasFrozenPlant() {
        return frozenPlant != null;
    }

    public boolean hasFrozenZombie() {
        return frozenZombie != null;
    }

    public int getIceHp() {
        return iceHp;
    }

    public void setIceHp(int iceHp) {
        this.iceHp = iceHp;
    }

    public Plant getFrozenPlant() {
        return frozenPlant;
    }

    public void setFrozenPlant(Plant frozenPlant) {
        this.frozenPlant = frozenPlant;
    }

    public Zombie getFrozenZombie() {
        return frozenZombie;
    }

    public void setFrozenZombie(Zombie frozenZombie) {
        this.frozenZombie = frozenZombie;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

}