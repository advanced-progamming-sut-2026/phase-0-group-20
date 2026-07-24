package models.entities.plants.strategy.category_strategy;

import models.entities.Sun;
import models.entities.SunType;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.game.GameSession;
import models.timeManager.TimeManager;

public class SunProductionStrategy implements IPlantStrategy {
    private int lastProductionTick = 0;
    private int aliveTicks = 0;
    private boolean hasProducedInstant = false;
    private boolean doubleSunChance = false;
    private int extraSunAmount = 0;
    private int growTimeReduction = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String plantName = context.getName();

        if (plantName.equals("Gold Bloom")) {
            if (!hasProducedInstant) {
                spawnSun(context, GameSession.getInstance(), SunType.HUGE_SUN, currentTick);

                if (extraSunAmount > 0) { // FOR UPGRADE
                    spawnCustomSun(context, GameSession.getInstance(), extraSunAmount, currentTick);
                }

                context.takeDamage(context.getCurrentHp());
                hasProducedInstant = true;
            }
            return;
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastProductionTick) >= intervalInTicks) {

            SunType type = getSunTypeForPlant(plantName, aliveTicks);
            spawnSun(context, GameSession.getInstance(), type, currentTick);

            if (doubleSunChance) { // for Upgrade
                // we can use random to randomly spawn double sun
                spawnSun(context, GameSession.getInstance(), type, currentTick);
            }

            lastProductionTick = currentTick;
        }
    }

    private void spawnSun(Plant context, GameSession gameSession, SunType type, int currentTick) {
        int spawnX = context.getPlacedTile().getCol();
        int spawnY = context.getPlacedTile().getRow();

        Sun newSun = new Sun(type, spawnX, spawnY);

        gameSession.getArena().addSun(newSun);
        gameSession.getTimeManager().registerNewTicker(newSun);

        notify(context.getName() + " produced a " + type.getLabel() + " sun! (Value: " + type.getValue() + ")");
    }

    private void spawnCustomSun(Plant context, GameSession gameSession, int amount, int currentTick) {
        int spawnX = context.getPlacedTile().getCol();
        int spawnY = context.getPlacedTile().getRow();

        Sun newSun = new Sun(amount, spawnX, spawnY);

        gameSession.getArena().addSun(newSun);
        gameSession.getTimeManager().registerNewTicker(newSun);
    }

    private SunType getSunTypeForPlant(String plantName, int aliveTicks) {
        return switch (plantName) {
            case "Sunflower" -> SunType.NORMAL_SUN;
            case "Twin Sunflower" -> SunType.SPECIAL_SUN;
            case "Primal Sunflower" -> SunType.LARGE_SUN;
            case "Sun-shroom" -> {
                int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;
                int stage3Threshold = Math.max(0, 72 - growTimeReduction);
                int stage2Threshold = Math.max(0, 24 - growTimeReduction);

                if (secondsAlive >= stage3Threshold) yield SunType.LARGE_SUN;
                if (secondsAlive >= stage2Threshold) yield SunType.NORMAL_SUN;
                yield SunType.TINY_SUN;
            }
            default -> SunType.NORMAL_SUN;
        };
    }

    public void setDoubleSunChance(boolean doubleSunChance) {
        this.doubleSunChance = doubleSunChance;
    }

    public void increaseSunAmount(float amount) {
        this.extraSunAmount += (int) amount;
    }

    public void reduceGrowTime(float seconds) {
        this.growTimeReduction += (int) seconds;
    }
}
