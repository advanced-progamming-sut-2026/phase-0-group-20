package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

public class NecromanceTile extends Tile {

    GraveStone graveStone = null;

    public NecromanceTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void onTick(int currentTick) {
        // nothing, the wave manager calls spawnZombieFromBelow at the start of each wave
    }

    public boolean canZombieEmerge() {
        return graveStone != null;
    }

    public void spawnZombieFromBelow(Zombie zombie) {
        if (zombie == null || !canZombieEmerge()) return;

        zombie.setRow(row);
        zombie.setX(col);
        GameSession session = GameSession.getInstance();
        session.getArena().addZombie(zombie);
        session.getTimeManager().registerNewTicker(zombie);
    }

    public void takeDamage(int damage) {
        if (graveStone == null) return;
        graveStone.takeDamage(damage);

        if (graveStone.getHp() <= 0) {
            System.out.println("Grave destroyed at row: " + row + ", col: " + col);
            GameSession session = GameSession.getInstance();
            if (graveStone.hasSun())
                session.addSun(50);

            if (graveStone.hasPlantFood())
                session.spawnPlantFood(row,col);

            this.graveStone = null;
        }
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        if (graveStone != null) return false;
        return super.isPlantable(plantToPlant);
    }

    public void setGraveStone(GraveStone graveStone) {
        this.graveStone = graveStone;
    }
}
