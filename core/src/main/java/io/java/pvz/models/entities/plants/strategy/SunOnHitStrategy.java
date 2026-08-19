package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class SunOnHitStrategy implements IPlantStrategy {
    private int lastRecordedHp = -1;
    private int sunPerHit = 5;

    private int totalSunDropped = 0;
    private final int MAX_SUN_DROPS = 3;
    private int cooldownTicks = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        if (lastRecordedHp == -1) {
            lastRecordedHp = context.getCurrentHp();
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        int currentHp = context.getCurrentHp();

        if (currentHp < lastRecordedHp) {
            lastRecordedHp = currentHp;

            if (cooldownTicks <= 0 && totalSunDropped < MAX_SUN_DROPS) {
                context.triggerAction("idle2");

                int spawnX = context.getPlacedTile().getCol();
                int spawnY = context.getPlacedTile().getRow();

                Sun newSun = new Sun(sunPerHit, spawnX, spawnY);

                newSun.setProducedByPlant(true);
                GameSession.getInstance().getArena().addSun(newSun);
                GameSession.getInstance().getTimeManager().registerNewTicker(newSun);

                totalSunDropped++;
                cooldownTicks = (int) (1.0f * TimeManager.TICKS_PER_SECOND);

                notify("☀️ " + context.getName() + " was bitten! Dropped " + sunPerHit + " sun.");
            }
        } else if (currentHp > lastRecordedHp) {
            lastRecordedHp = currentHp;
        }
    }

    public void addSunPerHitMultiplier(int extraSun) {
        this.sunPerHit += extraSun;
    }
}
