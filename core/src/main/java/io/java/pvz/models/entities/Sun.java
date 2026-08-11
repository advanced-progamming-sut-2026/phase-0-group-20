package io.java.pvz.models.entities;

import io.java.pvz.models.Position;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;

public class Sun implements Ticker {
    private int amountProduced;

    // don't forget to use position beautiful boys

    private SunType type;
    private Position position;
    private int fallTicksLeft = 50;
    private boolean isFalling = true;
    private boolean isCollected = false;
    private boolean exploded = false;
    private boolean isBeingAbsorbed = false;
    private int absorbedTicksCounter = 0;
    private boolean producedByPlant = false;

    public Sun(SunType type, int col, int row) {
        this.type = type;
        this.position = new Position(col, row);
        this.amountProduced = type.getValue();
    }

    public Sun(int amount, int col, int row) {
        this.type = null;
        this.position = new Position(col, row);
        this.amountProduced = amount;
    }

    @Override
    public void onTick(int currentTick) {
        if (isCollected) return;

        if (isBeingAbsorbed) {
            absorbedTicksCounter++;
            return;
        }

        if (!isFalling) return;

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
        notify(String.format("Sun reached the ground at position (%d, %d)\n", getCol() + 1, getRow() + 1));
    }

    public void collect() {
        this.isCollected = true;

        if (isFalling && type == SunType.RADIOACTIVE_SUN) {
            notify("Radioactive sun exploded mid-air!");
        } else {
            GameSession.getInstance().addSun(amountProduced);
        }
    }

    public void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
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

    public Position getPosition() {
        return position;
    }

    public boolean isBeingAbsorbed() {
        return isBeingAbsorbed;
    }

    public void setBeingAbsorbed(boolean beingAbsorbed) {
        this.isBeingAbsorbed = beingAbsorbed;
        if (!beingAbsorbed) {
            this.absorbedTicksCounter = 0;
        }
    }

    public int getAbsorbedTicksCounter() {
        return absorbedTicksCounter;
    }

    public void setFalling(boolean falling) {
        this.isFalling = falling;
    }

    public boolean isProducedByPlant() {
        return producedByPlant;
    }

    public void setProducedByPlant(boolean producedByPlant) {
        this.producedByPlant = producedByPlant;
        if (producedByPlant) {
            this.isFalling = false;
        }
    }
}
