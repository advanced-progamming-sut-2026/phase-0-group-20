package models.fields;


import models.timeManager.Ticker;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

public class Brain implements Ticker {

    private final int row;
    private int hp = 100;
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
