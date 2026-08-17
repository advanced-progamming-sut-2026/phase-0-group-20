package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.context.AllStarContext;
import io.java.pvz.models.entities.zombies.behavior.effect.AllStarTackleEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.Effect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

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

        if (zombie.getActiveEffects() != null) {
            for (ZombieEffect effect : zombie.getActiveEffects()) {
                if (effect instanceof AllStarTackleEffect) {
                    return;
                }
            }
        }

        GameSession session = GameSession.getInstance();

        for (Zombie z : session.getArena().getActiveZombies()) {
            if (z.isHypnotized() && !z.isDead() && z.getRow() == zombie.getRow() &&
                Math.abs(z.getX() - zombie.getX()) < 30) {
                zombie.addEffect(new AllStarTackleEffect(zombie, context, z));
                return;
            }
        }

        Tile currentTile = session.getArena().getTile(zombie.getRow(), zombie.getCol());
        if (currentTile != null) {
            List<Plant> targets = currentTile.getPlants();
            if (!targets.isEmpty() && !targets.get(0).isDead()) {
                zombie.addEffect(new AllStarTackleEffect(zombie, context, targets));
                return;
            }
        }

        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
