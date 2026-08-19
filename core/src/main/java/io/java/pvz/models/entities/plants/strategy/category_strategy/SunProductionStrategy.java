package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog; // 🌟 ایمپورت کاتالوگ برای خواندن طول انیمیشن

public class SunProductionStrategy implements IPlantStrategy {
    private int lastProductionTick = 0;
    private int aliveTicks = 0;

    // 🌟 متغیرهای زمان‌بندی برای تأخیر در تولید خورشید (سینک با انیمیشن)
    private int pendingSunSpawnTick = -1;
    private SunType pendingSunType = null;

    // 🌟 وضعیت اختصاصی برای Gold Bloom
    private int goldBloomState = 0; // 0: idling, 1: attacking, 2: dead
    private int actionDelayTick = -1;

    private boolean doubleSunChance = false;
    private int extraSunAmount = 0;
    private int growTimeReduction = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String plantName = context.getName();

        if (plantName.equals("Gold Bloom")) {
            handleGoldBloom(context, currentTick);
            return;
        }

        if (pendingSunSpawnTick != -1 && currentTick >= pendingSunSpawnTick) {
            spawnSun(context, GameSession.getInstance(), pendingSunType);
            if (doubleSunChance) {
                spawnSun(context, GameSession.getInstance(), pendingSunType);
            }
            pendingSunSpawnTick = -1;
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastProductionTick) >= intervalInTicks && pendingSunSpawnTick == -1) {

            SunType type = getSunTypeForPlant(plantName, aliveTicks, context);

            String actionName = plantName.equals("Sun-shroom") ? "special_stage" + context.getSize() : "special";
            context.triggerAction(actionName);

            float animDuration = 1.0f;
            AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(context);
            if (anim != null && anim.hasClip(actionName)) {
                animDuration = anim.getDuration(actionName);
            }

            pendingSunSpawnTick = currentTick + (int) (animDuration * TimeManager.TICKS_PER_SECOND);
            pendingSunType = type;

            lastProductionTick = currentTick;
        }
    }

    private void handleGoldBloom(Plant context, int currentTick) {
        if (goldBloomState == 2) return;

        int waitTicks = (int) (6.2f * TimeManager.TICKS_PER_SECOND);
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
        if (intervalInTicks > 0) waitTicks = intervalInTicks;

        if (goldBloomState == 0) {
            if (aliveTicks >= waitTicks) {
                context.triggerAction("attack");

                float animDuration = 2.4f;
                AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(context);
                if (anim != null && anim.hasClip("attack")) {
                    animDuration = anim.getDuration("attack");
                }

                actionDelayTick = currentTick + (int) (animDuration * TimeManager.TICKS_PER_SECOND);
                goldBloomState = 1;
            }
        } else if (goldBloomState == 1) {
            if (currentTick >= actionDelayTick) {
                spawnSun(context, GameSession.getInstance(), SunType.HUGE_SUN);

                if (extraSunAmount > 0) {
                    spawnCustomSun(context, GameSession.getInstance(), extraSunAmount);
                }

                context.takeDamage(context.getCurrentHp());
                goldBloomState = 2;
            }
        }
    }

    private void spawnSun(Plant context, GameSession gameSession, SunType type) {
        int spawnX = context.getPlacedTile().getCol();
        int spawnY = context.getPlacedTile().getRow();

        Sun newSun = new Sun(type, spawnX, spawnY);
        newSun.setProducedByPlant(true);

        gameSession.getArena().addSun(newSun);
        gameSession.getTimeManager().registerNewTicker(newSun);

        notify(context.getName() + " produced a " + type.getLabel() + " sun! (Value: " + type.getValue() + ")");
    }

    private void spawnCustomSun(Plant context, GameSession gameSession, int amount) {
        int spawnX = context.getPlacedTile().getCol();
        int spawnY = context.getPlacedTile().getRow();

        Sun newSun = new Sun(amount, spawnX, spawnY);
        newSun.setProducedByPlant(true);

        gameSession.getArena().addSun(newSun);
        gameSession.getTimeManager().registerNewTicker(newSun);
    }

    public SunType getSunTypeForPlant(String plantName, int aliveTicks, Plant context) {
        return switch (plantName) {
            case "Sunflower" -> SunType.NORMAL_SUN;
            case "Twin Sunflower" -> SunType.SPECIAL_SUN;
            case "Primal Sunflower" -> SunType.LARGE_SUN;
            case "Sun-shroom" -> {
                int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;
                int stage3Threshold = Math.max(0, 72 - growTimeReduction);
                int stage2Threshold = Math.max(0, 24 - growTimeReduction);

                if (secondsAlive >= stage3Threshold) {
                    if (context.getSize() < 3) {
                        context.setSize(3);
                        context.triggerAction("growth_stage2");
                    }
                    yield SunType.LARGE_SUN;
                }
                if (secondsAlive >= stage2Threshold) {
                    if (context.getSize() < 2) {
                        context.setSize(2);
                        context.triggerAction("growth_stage1");
                    }
                    yield SunType.NORMAL_SUN;
                }
                yield SunType.TINY_SUN;
            }
            default -> SunType.NORMAL_SUN;
        };
    }

    public void setDoubleSunChance(boolean doubleSunChance) { this.doubleSunChance = doubleSunChance; }
    public void increaseSunAmount(float amount) { this.extraSunAmount += (int) amount; }
    public void reduceGrowTime(float seconds) { this.growTimeReduction += (int) seconds; }
}
