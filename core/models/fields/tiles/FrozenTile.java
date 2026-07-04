package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantTag;
import models.game.GameSession;


public class FrozenTile extends Tile {
    public static final int MELT_RATE_PER_TICK = 6;

    private int iceHp = 600;
    private Plant frozenPlant;
    private Zombie frozenZombie;

    public FrozenTile(int row, int col) {
        super(row, col);
    }

    public FrozenTile(int row, int col, Plant frozenPlant) {
        super(row, col);
        this.frozenPlant = frozenPlant;
    }

    public FrozenTile(int row, int col, Zombie frozenZombie) {
        super(row, col);
        this.frozenZombie = frozenZombie;
    }

    @Override
    public void onTick(int currentTick) {
        if (hasAdjacentFirePlant())
            takeDamage(MELT_RATE_PER_TICK);
    }

    public void melt() {
        GameSession session = GameSession.getInstance();
        Tile newTile = new NormalTile(row, col);
        GameSession.getInstance().getArena().changeTile(row, col, newTile);

        if (frozenPlant != null) {
            frozenPlant.setPlacedTile(newTile);
            newTile.addPlant(frozenPlant);
            session.getArena().addPlant(frozenPlant);
            session.getTimeManager().registerNewTicker(frozenPlant);
            frozenPlant = null;
        }

        if (frozenZombie != null) {
            frozenZombie.setRow(row);
            frozenZombie.setX(col);
            session.getArena().addZombie(frozenZombie);
            session.getTimeManager().registerNewTicker(frozenZombie);
            frozenZombie = null;
        }
        System.out.println("Ice melted at [" + row + "][" + col + "]");
    }

    public void takeDamage(int amount) {
        iceHp -= amount;
        if (iceHp <= 0) melt();
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

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        return false;
    }
}
