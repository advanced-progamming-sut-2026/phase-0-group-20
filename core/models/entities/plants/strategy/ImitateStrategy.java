package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.TimeManager;

public class ImitateStrategy implements IPlantStrategy {
    private static final int TRANSFORM_DELAY_TICKS = 2 * TimeManager.TICKS_PER_SECOND;
    private int startTick = -1;
    private int targetPlantId = -1; // plant id

    private boolean autoPlantFood = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (targetPlantId == -1) return;

        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= TRANSFORM_DELAY_TICKS) {
            Tile currentTile = context.getPlacedTile();

            Plant transformedPlant = PlantFactory.create(targetPlantId); //bug?

            if (transformedPlant != null) {
                transformedPlant.setPlacedTile(currentTile);
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
