package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class ImitateStrategy implements IPlantStrategy {
    private static final int TRANSFORM_DELAY_TICKS = 2 * TimeManager.TICKS_PER_SECOND;
    private int startTick = -1;
    private int targetPlantId = -1;

    private boolean autoPlantFood = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (targetPlantId == -1) return;

        if (startTick == -1) {
            startTick = currentTick;
            context.triggerAction("attack");
        }

        if (currentTick - startTick >= TRANSFORM_DELAY_TICKS) {
            Tile currentTile = context.getPlacedTile();

            context.takeDamage(context.getCurrentHp());

            Plant transformedPlant = PlantFactory.create(targetPlantId);

            if (transformedPlant != null) {
                transformedPlant.setPlacedTile(currentTile);
                GameSession.getInstance().getTimeManager().registerNewTicker(transformedPlant);
                GameSession.getInstance().getArena().addPlant(transformedPlant);

                notify("🎭 Imitater transformed into " + transformedPlant.getName() + "!");

                if (autoPlantFood) {
                    notify("✨ Imitater used an automatic Plant Food!");
                    transformedPlant.useFood();
                }
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    public void setAutoPlantFood(boolean autoPlantFood) {
        this.autoPlantFood = autoPlantFood;
    }

    public void setTargetPlantId(int targetPlantId) {
        this.targetPlantId = targetPlantId;
    }
}
