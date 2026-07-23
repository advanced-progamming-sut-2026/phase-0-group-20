package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.context.ExplorerContext;
import models.enums.plants.PlantTag;
import models.game.GameSession;

import java.util.List;

public class TorchBurnAttack implements AttackBehavior {
    private final Zombie zombie;
    private final ExplorerContext context;
    private final AttackBehavior normalAttack;

    public TorchBurnAttack(Zombie zombie, ExplorerContext context) {
        this.zombie = zombie;
        this.context = context;
        this.normalAttack = new NormalAttack(zombie);
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        List<Plant> targetsPlant = session.getArena().getTile(zombie.getRow(), zombie.getCol()).getPlants();

        for (Plant plant : targetsPlant) {
            if (plant.getTags().contains(PlantTag.ICE)) {
                context.setTorchLit(false);
                notify("Explorer's torch was extinguished by an ICE plant!");
            } else if (plant.getTags().contains(PlantTag.FIRE)) {
                context.setTorchLit(true);
                notify("Explorer's torch was ignited by a FIRE plant!");
            }

            if (context.isTorchLit()) {
                plant.takeDamage(99999);
                notify("Explorer burnt " + plant.getName() + " to ashes!");

                resumeWalking();
            } else {
                normalAttack.execute();
            }
        }
        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
