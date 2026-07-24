package models.entities.zombies.behavior.attack;

import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.context.TurquoiseContext;
import models.enums.PhysicalConstants;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LaserAttack implements AttackBehavior {
    private final Zombie zombie;
    private final TurquoiseContext context;
    private final Random random = new Random();
    private static final int LASER_DAMAGE = 4000;

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

    private void stealSun(int targetAmount) {
        boolean stoleSomething = false;
        int actualStolen = 0;

        List<Sun> activeSuns = GameSession.getInstance().getArena().getActiveSuns();
        if (!activeSuns.isEmpty()) {
            int index = random.nextInt(activeSuns.size());
            Sun stolenSun = activeSuns.get(index);
            activeSuns.remove(index);
            GameSession.getInstance().getTimeManager().unregisterTicker(stolenSun);

            actualStolen = 50;
            stoleSomething = true;
        } else {
            int currentBank = GameSession.getInstance().getCurrentSun();
            if (currentBank >= targetAmount) {
                GameSession.getInstance().useSun(targetAmount);
                actualStolen = targetAmount;
                stoleSomething = true;
            } else if (currentBank > 0) {
                GameSession.getInstance().useSun(currentBank);
                actualStolen = currentBank;
                stoleSomething = true;
            }
        }

        if (stoleSomething) {
            context.addStolenSun(actualStolen);
            notify("Turquoise Skull stole " + actualStolen + " suns!");
        }
    }

    private void fireLaser() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = (int) (zombie.getX() / PhysicalConstants.TILE_UNIT_LENGTH);

        List<Plant> targets = new ArrayList<>();
        for (Plant p : session.getArena().getActivePlants()) {
            int pRow = p.getPlacedTile().getRow();
            int pCol = p.getPlacedTile().getCol();
            if (pRow == zRow && pCol <= zCol && pCol >= zCol - 4) {
                targets.add(p);
            }
        }

        for (Plant p : targets) {
            p.takeDamage(LASER_DAMAGE);
        }
        notify("Turquoise Skull fired a deadly laser!");

        context.reset();
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
