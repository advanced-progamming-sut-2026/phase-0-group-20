package io.java.pvz.models.quest.conditions;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;

public class EmptyLineCondition extends QuestCondition {
    int row;
    int col;
    boolean isRow = false;
    boolean isCol = false;
    boolean lost = false;
    boolean levelCompleted = false;

    public EmptyLineCondition(int row, int col) {
        this.row = row;
        this.col = col;
        isRow = (row != -1) ? true : false;
        isCol = (col != -1) ? true : false;
    }

    public EmptyLineCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();

        if (event == GameEvent.PLANT_PLACED && !lost) {
            Plant plant = payload.getPlant();
            int plantRow = plant.getPlacedTile().getRow();
            int plantCol = plant.getPlacedTile().getCol();

            if (isRow && plantRow == this.row) {
                lost = true;
            }

            if (isCol && plantCol == this.col) {
                lost = true;
            }
        } else if (event == GameEvent.LEVEL_COMPLETED) {
            levelCompleted = true;
        }
    }

    @Override
    public boolean isHappened() {
        return levelCompleted && !lost;
    }
}
