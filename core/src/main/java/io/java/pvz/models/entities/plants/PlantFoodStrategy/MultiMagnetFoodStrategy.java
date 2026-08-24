package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class MultiMagnetFoodStrategy implements PlantFoodStrategy {

    private int durationTicks = 0;
    private boolean executed = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;

        float animDuration = 1.0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) animDuration = anim.getDuration("plantfood_on") +
                anim.getDuration("plantfood");
            else if (anim.hasClip("plantfood")) animDuration = anim.getDuration("plantfood");
        }
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            GameSession gameSession = GameSession.getInstance();
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            double range = 15.0;

            List<Zombie> targets = gameSession.getArena().getZombiesInRadius(col, row, range);

            for (Zombie zombie : targets) {
                if (zombie.isDead() || zombie.getRow() != row) continue;

                for (Armor armor : zombie.getArmorPieces()) {
                    if (!armor.isDestroyed() && armor.isMetallic())
                        armor.takeDamage(99999);

                    if (!zombie.getArmorPieces().isEmpty())
                        zombie.getArmorPieces().clear();
                }
            }
            executed = true;
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.executed = false;
    }
}
