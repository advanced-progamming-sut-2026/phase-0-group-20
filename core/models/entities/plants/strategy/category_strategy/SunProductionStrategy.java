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

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String plantName = context.getName();

        if (plantName.equals("Gold Bloom")) {
            if (!hasProducedInstant) {
                spawnSun(context, gameSession, SunType.HUGE_SUN, currentTick);
                context.takeDamage(context.getCurrentHp());
                hasProducedInstant = true;
            }
            return;
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastProductionTick) >= intervalInTicks) {

            SunType type = getSunTypeForPlant(plantName, aliveTicks);
            spawnSun(context, gameSession, type, currentTick);

            lastProductionTick = currentTick;
        }
    }

    private void spawnSun(Plant context, GameSession gameSession, SunType type, int currentTick) {
        int spawnX = context.getPlacedTile().getCol();
        int spawnY = context.getPlacedTile().getRow();

        Sun newSun = new Sun(type, spawnX, spawnY, currentTick);

        gameSession.getArena().addSun(newSun);
        gameSession.getTimeManager().registerNewTicker(newSun);

        System.out.println(context.getName() + " produced a " + type.getLabel() + " sun! (Value: " + type.getValue() + ")");
    }

    private SunType getSunTypeForPlant(String plantName, int aliveTicks) {
        return switch (plantName) {
            case "Sunflower" -> SunType.NORMAL_SUN;
            case "Twin Sunflower" -> SunType.SPECIAL_SUN;
            case "Primal Sunflower" -> SunType.LARGE_SUN;
            case "Sun-shroom" -> {
                int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;
                if (secondsAlive >= 72) yield SunType.LARGE_SUN;
                if (secondsAlive >= 24) yield SunType.NORMAL_SUN;
                yield SunType.TINY_SUN;
            }
            default -> SunType.NORMAL_SUN;
        };
    }
}
