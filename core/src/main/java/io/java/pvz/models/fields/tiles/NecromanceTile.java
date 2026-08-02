package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.fields.obstacle.GraveHolder;
import io.java.pvz.models.fields.obstacle.GraveStone;
import io.java.pvz.models.game.GameSession;

public class NecromanceTile extends Tile implements GraveHolder {

    GraveStone graveStone = null;

    public NecromanceTile(int row, int col) {
        super(row, col);
    }

    @Override
    public void customTick(int currentTick) {
        // nothing, the wave manager calls spawnZombieFromBelow at the start of each wave
    }

    public boolean canZombieEmerge() {
        return graveStone != null;
    }

    public void spawnZombieFromBelow(Zombie zombie) {
        if (zombie == null || !canZombieEmerge()) return;

        zombie.setRow(position.getRow());
        zombie.setCol(position.getCol());
        GameSession session = GameSession.getInstance();
        session.getArena().addZombie(zombie);
        session.getTimeManager().registerNewTicker(zombie);
    }

    @Override
    public boolean isPlantable(Plant plantToPlant) {
        boolean isGraveBuster = (plantToPlant.getName().contains("Buster")|| plantToPlant.getName().contains("buster"));
        if (graveStone != null) return isGraveBuster;
        return super.isPlantable(plantToPlant);
    }

    @Override
    public GraveStone getGraveStone() {
        return graveStone;
    }

    public void setGraveStone(GraveStone graveStone) {
        this.graveStone = graveStone;
    }

    @Override
    public void removeGrave() {
        graveStone = null;
    }
}
