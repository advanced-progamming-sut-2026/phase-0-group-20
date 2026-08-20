package io.java.pvz.models.fields;


import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;

public class Brain implements Ticker {

    private final int row;
    private int hp = 500;
    private boolean isEaten;

    public Brain(int row) {
        this.row = row;
        this.isEaten = false;
    }

    public void eat() {
        this.isEaten = true;
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("A brain in row " + row + " was eaten! Ahh such a delicious brain !")
                        .build());
    }

    public boolean isEaten() {
        return isEaten;
    }

    public int getRow() {
        return row;
    }

    public void takeDamage(int amount) {
        if (isEaten) return;
        this.hp -= amount;
    }

    @Override
    public void onTick(int currentTick) {
        if (hp <= 0 && !isEaten)
            eat();
    }


}
