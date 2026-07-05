package models.entities;

import models.game.GameSession;
import models.timeManager.Ticker;

public class PlantFood implements Ticker {
    private int row;
    private int col;

    private int lifeTicksLeft = 200;
    private boolean isCollected = false;
    private boolean isExpired = false;

    public PlantFood(int row, int col) {
        this.row = row;
        this.col = col;
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