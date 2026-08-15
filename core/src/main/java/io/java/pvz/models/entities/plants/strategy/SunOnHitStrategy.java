package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.GameSession;

public class SunOnHitStrategy implements IPlantStrategy {
    private int lastRecordedHp = -1;
    private int sunPerHit = 5;

    @Override
    public void execute(Plant context, int currentTick) {
        if (lastRecordedHp == -1) {
            lastRecordedHp = context.getCurrentHp();
            return;
        }

        int currentHp = context.getCurrentHp();

        if (currentHp < lastRecordedHp) {

            context.triggerAction("idle2");

            int spawnX = context.getPlacedTile().getCol();
            int spawnY = context.getPlacedTile().getRow();

            Sun newSun = new Sun(sunPerHit, spawnX, spawnY);
            GameSession.getInstance().getArena().addSun(newSun);
            GameSession.getInstance().getTimeManager().registerNewTicker(newSun);

            lastRecordedHp = currentHp;
            notify("☀️ " + context.getName() + " was bitten! Dropped " + sunPerHit + " sun.");
        } else if (currentHp > lastRecordedHp) {
            lastRecordedHp = currentHp;
        }
    }

    public void addSunPerHitMultiplier(int extraSun) {
        this.sunPerHit += extraSun;
    }
}
