package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.obstacle.GraveHolder;
import models.fields.obstacle.GraveStone;
import models.game.GameSession;

public class NecromanceTile extends Tile implements GraveHolder {

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

        zombie.setRow(position.getRow());
        zombie.setCol( position.getCol());
        GameSession session = GameSession.getInstance();
        session.getArena().addZombie(zombie);
        session.getTimeManager().registerNewTicker(zombie);
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        if (graveStone != null) return false;
        return super.isPlantable(plantToPlant);
    }

    public void setGraveStone(GraveStone graveStone) {
        this.graveStone = graveStone;
    }

    @Override
    public GraveStone getGraveStone() {
        return graveStone;
    }

    @Override
    public void removeGrave() {
        graveStone = null;
    }
}
