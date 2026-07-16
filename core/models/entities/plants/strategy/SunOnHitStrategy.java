package models.entities.plants.strategy;

import models.entities.Sun;
import models.entities.plants.Plant;
import models.game.GameSession;

/**
 * Sun On Hit Strategy:
 * Used for Sun Bean. Whenever a zombie damages this plant (e.g., takes a bite),
 * it generates a specific amount of sun (e.g., 5 sun per hit).
 */

public class SunOnHitStrategy implements IPlantStrategy {
    private int lastRecordedHp = -1;
    private int sunPerHit = 5;

    @Override
    public void execute(Plant context, int currentTick) {
        // Initialize the HP tracker on the first tick
        if (lastRecordedHp == -1) {
            lastRecordedHp = context.getCurrentHp();
            return;
        }

        int currentHp = context.getCurrentHp();

        // Check if the plant has taken damage since the last tick
        if (currentHp < lastRecordedHp) {

            int spawnX = context.getPlacedTile().getCol();
            int spawnY = context.getPlacedTile().getRow();

            Sun newSun = new Sun(sunPerHit, spawnX, spawnY, currentTick);
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
