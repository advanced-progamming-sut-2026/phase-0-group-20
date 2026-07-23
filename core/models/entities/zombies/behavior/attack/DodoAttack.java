package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.game.GameSession;

import java.util.List;

public class DodoAttack implements AttackBehavior {
    private final Zombie zombie;
    private final AttackBehavior normalEatAttack;

    public DodoAttack(Zombie zombie) {
        this.zombie = zombie;
        this.normalEatAttack = new NormalAttack(zombie);
    }


    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();

        List<Plant> targetPlants = session.getArena().getTile(zombie.getRow(), zombie.getCol()).getPlants();
        if (targetPlants.isEmpty()) {
            zombie.setAttacking(false);
            zombie.setState(ZombieState.WALKING);
        }

        Plant targetPlant = targetPlants.getFirst();

        String pName = targetPlant.getName().toLowerCase();

        if (pName.equals("tall-nut")) {
            notify("Dodo Rider is blocked by Tall-nut!");
            normalEatAttack.execute();
            return;
        }

        if (isFlyable(targetPlant)) {
            notify("Dodo Rider flew over " + targetPlant.getName() + "!");

            int newCol = targetPlant.getPlacedTile().getCol() - 1;
            if (newCol >= 0) {
                zombie.setCol(newCol);
                zombie.setX(zombie.getX() - 10);
            }

            zombie.setAttacking(false);
            zombie.setState(ZombieState.WALKING);
        } else {
            normalEatAttack.execute();
        }
    }

    private boolean isFlyable(Plant plant) {
        PlantCategory category = plant.getCategory();

        boolean isHighHp = category == PlantCategory.WALL_NUT;

        boolean isTrap = plant.getTags().contains(PlantTag.TRAP);

        boolean isLaneChanger = plant.getTags().contains(PlantTag.MOVE_ZOMBIES);

        return isHighHp || isTrap || isLaneChanger;
    }
}
