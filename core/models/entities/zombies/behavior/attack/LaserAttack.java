package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class LaserAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int laserDamage;
    private int channelTicks = 0;
    private int stolenSuns = 0;

    public LaserAttack(Zombie zombie, int laserDamage) {
        this.zombie = zombie;
        this.laserDamage = laserDamage;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        channelTicks++;

        if (channelTicks % 10 == 0 && channelTicks <= 50) {
            int currentSun = session.getCurrentSun();
            int sunToSteal = Math.min(currentSun, 25);

            session.setCurrentSun(currentSun - sunToSteal);
            stolenSuns += sunToSteal;
            notify(zombie.getName() + " stole " + sunToSteal + " suns! Total stolen: " + stolenSuns);
        }

        if (channelTicks >= 50) {
            shootLaser(session);

            channelTicks = 0;
            zombie.setAttacking(false);
            zombie.setState(ZombieState.WALKING);
        }
    }

    private void shootLaser(GameSession session) {
        int row = zombie.getRow();
        List<Plant> toDestroy = new ArrayList<>();

        for (Plant plant : session.getArena().getActivePlants()) {
            if (plant.getPlacedTile() != null && plant.getPlacedTile().getRow() == row) {
                int pCol = plant.getPlacedTile().getCol();
                int distance = zombie.getCol() - pCol;

                if (distance >= 0 && distance <= 4) {
                    toDestroy.add(plant);
                }
            }
        }

        for (Plant plant : toDestroy) {
            plant.takeDamage(laserDamage);
            notify(zombie.getName() + " blasted " + plant.getName() + " with a laser!");
        }
    }

    public int getStolenSuns() {
        return stolenSuns;
    }
}
