package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

public class MeleeStrategy implements IPlantStrategy {
    private int lastAttackTick = 0;
    private int aliveTicks = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String name = context.getName();

        if (name.equals("Chomper")) return;

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastAttackTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean attacked = false;

            if (name.equals("Bonk Choy") || name.equals("Wasabi Whip")) {
                Zombie target = null;
                double minDistance = Double.MAX_VALUE;

                for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                    if (z.isDead()) continue;
                    double dist = z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol;

                    if (dist >= -1.0 && dist <= 1.5) {
                        if (Math.abs(dist) < minDistance) {
                            minDistance = Math.abs(dist);
                            target = z;
                        }
                    }
                }

                if (target != null) {
                    int damage = name.equals("Bonk Choy") ? 15 : 40;
                    target.takeDamage(damage);
                    attacked = true;

                    if (name.equals("Wasabi Whip")) {
                        target.removeChillEffect();
                        target.removeFreezeEffect();
                    }
                    System.out.println("🥊 " + name + " landed a melee strike on " + target.getName() + "!");
                }
            } else if (name.equals("Phat Beet") || name.equals("Kiwibeast")) {
                List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5);

                if (!targets.isEmpty()) {
                    int damage = 15;

                    if (name.equals("Kiwibeast")) {
                        int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;
                        if (secondsAlive >= 72) damage = 45;
                        else if (secondsAlive >= 24) damage = 30;
                    }

                    for (Zombie z : targets) {
                        if (!z.isDead()) {
                            z.takeDamage(damage);
                            attacked = true;
                        }
                    }
                    if (attacked) {
                        System.out.println("🔊 " + name + " slammed a 3x3 area, hitting " + targets.size() + " zombies!");
                    }
                }
            }
            if (attacked) {
                lastAttackTick = currentTick;
            }
        }
    }
}
