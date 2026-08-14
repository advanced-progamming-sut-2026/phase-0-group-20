package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.AntiJumpStrategy;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.effect.DodoFlyEffect;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.game.GameSession;

import java.util.List;

public class DodoMove implements MoveBehavior {
    private final Zombie zombie;

    public DodoMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        if (zombie.getState() == ZombieState.FLY_START ||
            zombie.getState() == ZombieState.FLYING ||
            zombie.getState() == ZombieState.FLY_END) {
            return;
        }

        int currentCol = zombie.getCol();
        int targetCol = Math.min (8, currentCol - 1);

        if (targetCol < 0) {
            zombie.moveForward();
            return;
        }

        float tileLeftEdge = (currentCol * PhysicalConstants.TILE_WIDTH) + PhysicalConstants.GRID_START_X;
        float jumpTriggerDistance = tileLeftEdge + 20f;

        if (zombie.getX() <= jumpTriggerDistance) {
            GameSession session = GameSession.getInstance();
            List<Plant> nextTilePlants = session.getArena().getTile(zombie.getRow(), targetCol).getPlants();

            if (!nextTilePlants.isEmpty()) {
                Plant targetPlant = nextTilePlants.getFirst();

                boolean isBlocked = false;
                for (IPlantStrategy s : targetPlant.getStrategies()) {
                    if (s instanceof AntiJumpStrategy) {
                        isBlocked = true;
                        break;
                    }
                }

                if (!isBlocked && isFlyable(targetPlant)) {
                    int landingCol = Math.max(0, targetCol - 1);

                    zombie.setAttacking(false);

                    zombie.addEffect(new DodoFlyEffect(zombie, landingCol));
                    return;
                }
            }
        }

        zombie.moveForward();
    }

    private boolean isFlyable(Plant plant) {
        PlantCategory category = plant.getCategory();
        boolean isHighHp = category == PlantCategory.WALL_NUT;
        boolean isTrap = plant.getTags().contains(PlantTag.TRAP);
        boolean isLaneChanger = plant.getTags().contains(PlantTag.MOVE_ZOMBIES);
        return isHighHp || isTrap || isLaneChanger;
    }
}
