package models.game.events;

import models.entities.plants.Plant;
import models.game.GameSession;
import models.timeManager.Ticker;

import java.util.HashMap;
import java.util.Map;

public class ScoreManager implements GameEventListener, Ticker {

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
        if (killsThisTick >= 2) {
            int comboBonus = killsThisTick * 25;
            totalMewpoints += comboBonus;
            GameSession.notify("Simultaneous Kill Combo! +" + comboBonus + " Mewpoints");
        }


        for (Map.Entry<Plant, Integer> entry : plantKillsThisTick.entrySet()) {
            if (entry.getValue() > 1) {
                int multiKillBonus = entry.getValue() * 50;
                totalMewpoints += multiKillBonus;
                GameSession.notify("Multikill! One plant killed " + entry.getValue() +
                        " zombies! +" + multiKillBonus + " Mewpoints");
            }
        }


        killsThisTick = 0;
        plantKillsThisTick.clear();
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {

        if (event == GameEvent.ZOMBIE_KILLED) {
            killsThisTick++;
            totalMewpoints += 10;

            if (payload.getZombie() != null && payload.getZombie().getCol() >= 5) {
                totalMewpoints += 20;
            }

            if (payload.getPlant() != null) {
                Plant killer = payload.getPlant();
                plantKillsThisTick.put(killer, plantKillsThisTick.getOrDefault(killer, 0) + 1);
            }

        } else if (event == GameEvent.ZOMBIE_KILLED_LAWN_MOWER) {

            totalMewpoints = Math.max(0, totalMewpoints - 50);

        } else if (event == GameEvent.SUN_COLLECTED) {

            totalMewpoints += 2;
        }
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
}