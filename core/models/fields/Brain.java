package models.fields;


import models.timeManager.Ticker;

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
        System.out.println("A brain in row " + row + " was eaten! Ahh such a delicious brain !");// print for now
    }

    public boolean isEaten() {
        return isEaten;
    }

    public int getRow() {
        return row;
    }

    @Override
    public void onTick(int currentTick) {
        if (hp <= 0)
            isEaten = true;

    }
}
