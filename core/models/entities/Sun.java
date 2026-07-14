package models.entities;

import models.Position;
import models.game.GameSession;
import models.timeManager.Ticker;

public class Sun implements Ticker {
    private int productionTime;
    private int amountProduced;

    // don't forget to use position beautiful boys

    private SunType type;
    private Position position;
    private int fallTicksLeft = 50;
    private boolean isFalling = true;
    private boolean isCollected = false;
    private boolean exploded = false;

    public Sun(SunType type, int col, int row, int currentTick) {
        this.type = type;
        this.position = new Position(col, row);
        this.productionTime = currentTick;
        this.amountProduced = type.getValue();
    }

    public Sun(int amount, int col, int row, int currentTick) {
        this.type = null;
        this.position = new Position(col, row);
        this.productionTime = currentTick;
        this.amountProduced = amount;
    }

    @Override
    public void onTick(int currentTick) {
        if (isCollected || !isFalling) return;

        fallTicksLeft--;
        if (fallTicksLeft <= 0) {
            reachGround();
        }
    }

    private void reachGround() {
        isFalling = false;

        if (this.type == SunType.RADIOACTIVE_SUN) {
            this.type = SunType.NORMAL_SUN;
            this.amountProduced = this.type.getValue();
        }

        System.out.printf("Sun reached the ground at position (%d, %d)\n", getCol(), getRow());
    }

    public void collect() {
        this.isCollected = true;

        if (isFalling && type == SunType.RADIOACTIVE_SUN) {
            System.out.println("Radioactive sun exploded mid-air!");
        } else {
            GameSession.getInstance().addSun(amountProduced);
        }
    }

    public int getCol() {
        return position.getCol();
    }

    public int getRow() {
        return position.getRow();
    }

    public boolean isFalling() {
        return isFalling;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public SunType getType() {
        return type;
    }

    public boolean isExploded() {
        return exploded;
    }

    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }
}
