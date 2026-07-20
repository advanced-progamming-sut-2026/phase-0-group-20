package models.quest.conditions;

import models.entities.plants.Plant;
import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

import java.util.List;

public class SymmetryLogicCondition extends QuestCondition {
    boolean needsSymmetry;
    boolean isHappened = false;

    public SymmetryLogicCondition(boolean needsSymmetry) {
        this.needsSymmetry = needsSymmetry;
        targetProgress = 1;
    }

    public SymmetryLogicCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        if (payload.getType() == GameEvent.LEVEL_COMPLETED) {
            boolean symmet = checkIfSymmetry(payload.getArena());
            isHappened = symmet == needsSymmetry;
        }
    }

    public boolean checkIfSymmetry(Arena arena) {
        int rows = arena.getRows();
        int cols = arena.getCols();

        for (int r = 0; r < rows / 2; r++) {
            int oppositeRow = rows - 1 - r;

            for (int c = 0; c < cols; c++) {
                Tile topTile = arena.getTile(r, c);
                Tile bottomTile = arena.getTile(oppositeRow, c);

                List<Plant> topPlants = topTile.getPlants();
                List<Plant> bottomPlants = bottomTile.getPlants();

                if (topPlants.size() != bottomPlants.size()) {
                    return false;
                }

                for (int i = 0; i < topPlants.size(); i++) {
                    String topPlantName = topPlants.get(i).getName();
                    String bottomPlantName = bottomPlants.get(i).getName();

                    if (!topPlantName.equals(bottomPlantName)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean isHappened() {
        return isHappened;
    }
}
