package com.Project.PVZ.models.entities.zombies.behavior.effect;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.game.GameSession;

public class JalapenoTimerEffect extends Effect {

    private static final int EXPLOSION_DELAY_TICKS = 100;

    public JalapenoTimerEffect(Zombie zombie) {
        super(zombie, EXPLOSION_DELAY_TICKS);
    }

    @Override
    public void onApply() {

    }

    @Override
    public void onRemove() {
        if (zombie == null || zombie.isDead()) return;

        GameSession session = GameSession.getInstance();
        int row = zombie.getRow();

        for (Plant p : session.getArena().getActivePlants())
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == row)
                p.takeDamage(Integer.MAX_VALUE);

        zombie.takeDamage(Integer.MAX_VALUE);
        GameSession.notify("Zombotany: Jalapeno Zombie exploded in row " + row + "!");
    }
}
