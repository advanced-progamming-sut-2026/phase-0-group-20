package models.timeManager;

import models.fields.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

public class TimeManager {
    public static final int TICKS_PER_SECOND = 10;


    private int currentTick;
    private List<Ticker> listeners;

    public TimeManager() {
        this.currentTick = 0;
        this.listeners = new ArrayList<>();
    }

    public static boolean isEveryNTicks(int currentTick, int n) {
        return currentTick % n == 0;
    }

    public void registerNewTicker(Ticker newTicker) {
        if (newTicker != null && !listeners.contains(newTicker))
            listeners.add(newTicker);
    }

    public void unregisterTicker(Ticker ticker) {
        listeners.remove(ticker);
    }

    public void advanceTime(int amount) {
        for (int i = 0; i < amount; i++) {
            currentTick++;
            checkPerTick();
        }
    }

    public void tick() {
        advanceTime(1);
    }

    private void checkPerTick() {
        List<Ticker> snapshot = new ArrayList<>(listeners);
        for (Ticker ticker : snapshot) {
            ticker.onTick(currentTick);
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getElapsedSeconds() {
        return currentTick / TICKS_PER_SECOND;
    }
}
