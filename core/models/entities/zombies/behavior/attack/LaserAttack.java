package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.context.TurquoiseContext;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class LaserAttack implements AttackBehavior {
    private final Zombie zombie;
    private final TurquoiseContext context;

    public LaserAttack(Zombie zombie, TurquoiseContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        if (!context.isCharging()) {
            context.startCharging();
            notify("Turquoise Skull is warming up its laser!");
        }

        context.incrementCharge();

        if (context.getChargeTicks() % 10 == 0) {
            stealSun(25);
        }

        if (context.getChargeTicks() >= 50) {
            fireLaser();
        }
    }

    private void stealSun(int amount) {
        GameSession.getInstance().useSun(amount);
        context.addStolenSun(amount);

         notify("Turquoise Skull stole " + amount + " suns!");
    }

    private void fireLaser() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = zombie.getCol();

        List<Plant> targets = new ArrayList<>();
        for (Plant p : session.getArena().getActivePlants()) {
            int pRow = p.getPlacedTile().getRow();
            int pCol = p.getPlacedTile().getCol();
            if (pRow == zRow && pCol <= zCol && pCol >= zCol - 4) {
                targets.add(p);
            }
        }

        for (Plant p : targets) {
            p.takeDamage(99999);
        }
        notify("Turquoise Skull fired a deadly laser!");

        context.reset();
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
