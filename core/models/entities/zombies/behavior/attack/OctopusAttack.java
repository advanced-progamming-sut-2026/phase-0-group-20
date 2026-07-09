package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.plants.effect.OctopusEffect;
import models.entities.plants.effect.PlantEffect;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OctopusAttack implements AttackBehavior {
    private final Zombie zombie;
    private final Random random = new Random();

    public OctopusAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        int row = zombie.getRow();
        List<Plant> validTargets = new ArrayList<>();

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == row) {
                boolean hasOctopus = false;
                for (PlantEffect effect : p.getActiveEffects()) {
                    if (effect instanceof OctopusEffect) {
                        hasOctopus = true;
                        break;
                    }
                }

                if (!hasOctopus) {
                    validTargets.add(p);
                }
            }
        }

        if (!validTargets.isEmpty()) {
            Plant target = validTargets.get(random.nextInt(validTargets.size()));

            target.addEffect(new OctopusEffect(600));
        }

        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
