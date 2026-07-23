package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.context.AllStarContext;
import models.game.GameSession;

import java.util.List;

public class AllStarSmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final AllStarContext context;
    private final AttackBehavior normalAttack;

    public AllStarSmashAttack(Zombie zombie, AllStarContext context) {
        this.zombie = zombie;
        this.context = context;
        this.normalAttack = new NormalAttack(zombie);
    }

    @Override
    public void execute() {
        if (context.hasTackled()) {
            normalAttack.execute();
            return;
        }

        GameSession session = GameSession.getInstance();
        boolean hitSomething = false;

        List<Plant> targets = session.getArena().getTile(zombie.getRow(), zombie.getCol()).getPlants();

        for (Plant plant : targets) {
            plant.takeDamage(99999);
            notify("All-Star Zombie tackled and destroyed " + plant.getName() + "!");
            hitSomething = true;
        }

        for (Zombie z : session.getArena().getActiveZombies()) {
            if (z.isHypnotized() && z.getRow() == zombie.getRow() && Math.abs(z.getX() - zombie.getX()) < 30) {
                z.takeDamage(99999);
                notify("All-Star Zombie tackled a hypnotized zombie!");
                hitSomething = true;
                break;
            }
        }

        if (hitSomething) {
            context.setTackled();
            resumeWalking();
        } else {
            resumeWalking();
        }

    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
