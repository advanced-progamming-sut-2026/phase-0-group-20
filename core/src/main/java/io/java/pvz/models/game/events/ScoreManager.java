package io.java.pvz.models.game.events;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.timeManager.Ticker;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager implements GameEventListener, Ticker {

    private static final int COMBO_BONUS_MULTIPLIER = 25;
    private static final int MULTI_KILL_BONUS_MULTIPLIER = 50;
    private static final int ZOMBIE_KILLED_BASE_POINTS = 10;
    private static final int LONGSHOT_POINTS = 40;
    private static final int LAWNMOWER_PENALTY_POINTS = 50;
    private static final int SUN_COLLECTED_POINTS = 2;
    private static final int LONGSHOT_MIN_COLUMN = 5;

    private final Map<Plant, Integer> plantKillsThisTick = new HashMap<>();
    private int totalMewpoints = 0;
    private int killsThisTick = 0;

    public ScoreManager() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        messenger.addListener(GameEvent.ZOMBIE_KILLED, this);
        messenger.addListener(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, this);
        messenger.addListener(GameEvent.SUN_COLLECTED, this);
    }

    @Override
    public void onTick(int currentTick) {
        processComboBonus();
        processMultiKillBonus();

        killsThisTick = 0;
        plantKillsThisTick.clear();
    }

    private void processComboBonus() {
        if (killsThisTick >= 2) {
            int comboBonus = killsThisTick * COMBO_BONUS_MULTIPLIER;
            totalMewpoints += comboBonus;
            notify("Combo Attack!!    +" + comboBonus);
        }
    }

    private void processMultiKillBonus() {
        for (Map.Entry<Plant, Integer> entry : plantKillsThisTick.entrySet()) {
            if (entry.getValue() > 1) {
                int multiKillBonus = entry.getValue() * MULTI_KILL_BONUS_MULTIPLIER;
                totalMewpoints += multiKillBonus;
                notify("Multi Kill!!    +" + multiKillBonus);
            }
        }
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.ZOMBIE_KILLED) {
            handleZombieKilled(payload);
        } else if (event == GameEvent.ZOMBIE_KILLED_LAWN_MOWER) {
            handleLawnMowerKilled();
        } else if (event == GameEvent.SUN_COLLECTED) {
            handleSunCollected();
        }
    }

    private void handleZombieKilled(GameEventPayload payload) {
        killsThisTick++;
        totalMewpoints += ZOMBIE_KILLED_BASE_POINTS;

        if (payload.getZombie() != null && payload.getZombie().getCol() >= LONGSHOT_MIN_COLUMN) {
            totalMewpoints += LONGSHOT_POINTS;
            notify("LONGSHOT!!!     +" + LONGSHOT_POINTS);
        }

        if (payload.getPlant() != null) {
            Plant killer = payload.getPlant();
            plantKillsThisTick.put(killer, plantKillsThisTick.getOrDefault(killer, 0) + 1);
        }
    }

    private void handleLawnMowerKilled() {
        totalMewpoints = Math.max(0, totalMewpoints - LAWNMOWER_PENALTY_POINTS);
        notify("LAWNMOWER Penalty!!     -" + LAWNMOWER_PENALTY_POINTS);
    }

    private void handleSunCollected() {
        totalMewpoints += SUN_COLLECTED_POINTS;
        notify("SUN COLLECTED!!!      +" + SUN_COLLECTED_POINTS);
    }

    public int getTotalMewpoints() {
        return totalMewpoints;
    }

    public void unregister() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        messenger.removeListener(GameEvent.ZOMBIE_KILLED, this);
        messenger.removeListener(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, this);
        messenger.removeListener(GameEvent.SUN_COLLECTED, this);
    }

    private void notify(String message) {
        GameEventMessenger.getInstance().dispatch(
            GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message(message)
                .build()
        );
    }
}
