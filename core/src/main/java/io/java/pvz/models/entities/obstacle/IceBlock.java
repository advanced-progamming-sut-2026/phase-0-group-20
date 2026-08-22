package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.effect.FreezeEffect;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

public class IceBlock extends PushableObstacle implements Ticker {
    public static final int MELT_RATE_PER_TICK = 60 / TimeManager.TICKS_PER_SECOND;
    private Plant frozenPlant;
    private Zombie frozenZombie;

    public IceBlock(Plant frozenPlant, int row, int col) {
        super(PushableObjectType.ICE_BLOCK, col, row, 100);
        this.frozenPlant = frozenPlant;
    }

    public IceBlock(Zombie frozenZombie, int row, int col) {
        super(PushableObjectType.ICE_BLOCK, col, row, 100);
        this.frozenZombie = frozenZombie;
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

            int dRow = Math.abs(plant.getPlacedTile().getRow() - position.getRow());
            int dCol = Math.abs(plant.getPlacedTile().getCol() - position.getCol());
            if (dRow <= 1 && dCol <= 1 && (dRow != 0 || dCol != 0)) return true;
        }
        return false;
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;

        if (health <= 0) {
            health = 0;
            melt();
        }
    }


    @Override
    public void onDestroy() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        Tile thisTile = session.getArena().getTile(position.getRow(), position.getCol());
        if (thisTile instanceof IceHolder holder) {
            holder.removeIceBlock();
        }

        if (frozenPlant != null) {
            frozenPlant.setFrozen(false);
            frozenPlant.getActiveEffects().removeIf(effect ->
                effect instanceof FreezeEffect);
            if (thisTile != null) thisTile.addPlant(frozenPlant);
            session.getArena().addPlant(frozenPlant);
            session.getTimeManager().registerNewTicker(frozenPlant);
            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("REMOVE_ICE_OVERLAY")
                    .plant(frozenPlant)
                    .build());
            frozenPlant = null;
        }

        if (frozenZombie != null) {
            frozenZombie.setRow(position.getRow());
            frozenZombie.setCol(position.getCol());
            session.getArena().addZombie(frozenZombie);
            session.getTimeManager().registerNewTicker(frozenZombie);
            frozenZombie = null;
        }
    }

    public void melt() {
        if (isDestroyed) return;
        isDestroyed = true;
        onDestroy();

        GameSession session = GameSession.getInstance();
        if (session != null) {
            if (session.getArena() != null) {
                session.getArena().getActiveObstacles().remove(this);
            }
            session.getTimeManager().unregisterTicker(this);
        }
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("IceBlock melted at [" + position.getRow() + "][" + position.getCol() + "]!")
                .build());
    }

    public Plant getFrozenPlant() {
        return frozenPlant;
    }
}
