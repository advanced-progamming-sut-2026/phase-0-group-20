package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.ChillEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class GlobalEffectStrategy implements IPlantStrategy {
    private static final int ACTIVATION_DELAY = TimeManager.TICKS_PER_SECOND;
    private float chillBonusDuration;
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) {
            startTick = currentTick;
            context.triggerAction("attack");
        }

        if (currentTick - startTick >= ACTIVATION_DELAY) {

            if (context.getName().equals("Ice-shroom")) {
                notify("❄️ Ice-shroom exploded and froze the entire board!");

                for (Zombie z : GameSession.getInstance().getArena().getActiveZombies()) {
                    if (!z.isDead()) {
                        boolean has = false;
                        for (ZombieEffect effect : z.getActiveEffects()) {
                            if (effect instanceof ChillEffect) {
                                has = true;
                                break;
                            }
                        }
                        if (has) continue;
                        int totalDurations = (int) (context.getAbilityValue() + chillBonusDuration);
                        z.addEffect(new ChillEffect(z, totalDurations));
                        notify("-> " + z.getName() + " is completely frozen! for " + totalDurations + " ticks");
                    }
                }
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    public void increaseFreezeDuration(float value) {
        chillBonusDuration += value;
    }
}
