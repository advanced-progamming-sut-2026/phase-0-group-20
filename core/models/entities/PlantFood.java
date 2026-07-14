package models.entities;

import models.Position;
import models.timeManager.Ticker;

public class PlantFood implements Ticker {
    private Position position;

    private int lifeTicksLeft = 200;
    private boolean isCollected = false;
    private boolean isExpired = false;

    public PlantFood(int row, int col) {
        position = new Position(row, col);
    }

    @Override
    public void onTick(int currentTick) {
        if (isCollected || isExpired) return;
        lifeTicksLeft--;
        if (lifeTicksLeft <= 0) this.isExpired = true;
    }

    public void collect() {
        if (isCollected || isExpired) return;
        this.isCollected = true;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public int getRow() {
        return position.getRow();
    }

    public void setRow(int row) {
        position.setRow(row);
    }

    public int getCol() {
        return position.getCol();
    }

    public void setCol(int col) {
        position.setCol(col);
    }
}