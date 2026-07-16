package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.timeManager.TimeManager;

import java.util.List;


/**
 * Trap Strategy:
 * The plant remains inactive (unarmed) for a specific duration after planting.
 * Once armed, it monitors its tile (or adjacent ones) and triggers a deadly effect
 * when an enemy steps on it.
 */

public class TrapStrategy implements IPlantStrategy {
    private int startTick = -1;
    private boolean isArmed = false;
    private boolean initialized = false;
    private int armingTimeTicks = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        String name = context.getName();

        if (!initialized) {
            if (name.equals("Potato Mine")) {
                armingTimeTicks = 15 * TimeManager.TICKS_PER_SECOND;
            } else if (name.equals("Primal Potato Mine")) {
                armingTimeTicks = 5 * TimeManager.TICKS_PER_SECOND;
            } else {
                armingTimeTicks = 0;
            }
            initialized = true;
        }
        if (startTick == -1) startTick = currentTick;

        if (!isArmed && (currentTick - startTick) >= armingTimeTicks) {
            isArmed = true;
            if (armingTimeTicks > 0) {
                notify("💣 " + name + " is now armed and ready!");
            }
        }

        if (!isArmed) return;

        int plantRow = context.getPlacedTile().getRow();
        double plantCol = context.getPlacedTile().getCol();
        Zombie target = null;

        double detectionRadius = name.equals("Squash") ? 1.5 : 0.5;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;

            double dist = Math.abs(z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol);
            if (dist <= detectionRadius) {
                target = z;
                break;
            }
        }

        if (target != null) {
            notify("🚨 " + name + " TRAP TRIGGERED!");

            switch (name) {
                case "Potato Mine":
                    target.takeDirectDamage(1800, context);
                    break;

                case "Primal Potato Mine":
                    List<Zombie> aoeTargets = GameSession.getInstance().getArena().getZombiesInRadius((int) plantCol, plantRow, 1.5);
                    for (Zombie z : aoeTargets) {
                        if (!z.isDead()) z.takeDirectDamage(2400, context);
                    }
                    notify("💥 Primal Potato Mine dealt massive AoE damage!");
                    break;

                case "Squash":
                    target.takeDirectDamage(1800, context);
                    notify("🪨 Squash crushed " + target.getName() + "!");
                    break;

                case "Tangle Kelp":
                    target.takeDirectDamage(9999, context);
                    notify("🌊 Tangle Kelp pulled " + target.getName() + " underwater!");
                    break;

                case "Iceberg Lettuce":
                    // freeze zombie
                    notify("❄️ Iceberg Lettuce completely froze " + target.getName() + "!");
                    break;
            }
            context.takeDamage(context.getCurrentHp());
            GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_LOST)
                    .plant(context)
                    .build();
            GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_LOST, payload);
        }

    }


    public void setArmingTimeTicks(int armingTime) {
        this.armingTimeTicks = armingTime;
    }

    public int getArmingTimeTicks() {
        return armingTimeTicks;
    }

    public void setArmed(boolean armed) {
        this.isArmed = armed;
    }

}
