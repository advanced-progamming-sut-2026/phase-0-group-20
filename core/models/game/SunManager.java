package models.game;

import models.entities.Sun;
import models.entities.SunType;
import models.timeManager.Ticker;

import java.util.Random;

public class SunManager implements Ticker {
    private final Arena arena;
    private final Random rand;
    private int timeToNextSun = 0;

    public SunManager(Arena arena) {
        this.arena = arena;
        this.rand = new Random();
        calculateTheNextSun(0);
    }

    @Override
    public void onTick(int currentTick) {
        timeToNextSun--;
        if (timeToNextSun == 0) {
            spawnSun(currentTick);
            calculateTheNextSun(currentTick);
        }
    }

    private void calculateTheNextSun(int currentTick) {
        int timeInSeconds = currentTick / 10;
        double nextSunInSeconds = Math.max(6 + (0.05 * timeInSeconds), 12.0);
        this.timeToNextSun = (int) (nextSunInSeconds * 10);
    }

    private void spawnSun(int currentTick) {
        SunType type = randomSunType();
        int spawnX = rand.nextInt(arena.getCols());
        int spawnY = rand.nextInt(arena.getRows());
        Sun sun = new Sun(type, spawnX, spawnY, currentTick);
        GameSession.getInstance().getTimeManager().registerNewTicker(sun);
        String message = "New sun reached from the sky at " + (spawnX + 1) + ", " + (spawnY + 1) + ".";
        GameSession.notify(message);
        arena.addSun(sun);


    }

    private SunType randomSunType() {
        int chance = rand.nextInt(100);
        if (chance <= 80) {
            return SunType.NORMAL_SUN;
        } else if (chance <= 95) {
            return SunType.SPECIAL_SUN;
        } else return SunType.RADIOACTIVE_SUN;
    }
}
