package models.quest.conditions;

import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class EmptyLineCondition extends QuestCondition {
    int row;
    int col;
    boolean isRow = false;
    boolean isCol = false;
    boolean lost = false;

    public EmptyLineCondition(int row, int col) {
        this.row = row;
        this.col = col;
        isRow = (row != -1) ? true : false;
        isCol = (col != -1) ? true : false;
    }

    public EmptyLineCondition(){};

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event != GameEvent.LEVEL_COMPLETED) return;
        Arena arena = payload.getArena();
        if (isRow) {
            for (Tile tile : arena.getTiles()[row]) {
                if (!tile.getPlants().isEmpty()) {
                    lost = true;
                    return;
                }
            }
        } else {
            for (Tile tile : arena.getTiles()[col]) {
                if (!tile.getPlants().isEmpty()) {
                    lost = true;
                    return;
                }
            }
        }

    }

    @Override
    public boolean isHappened() {
        return !lost;
    }
}
