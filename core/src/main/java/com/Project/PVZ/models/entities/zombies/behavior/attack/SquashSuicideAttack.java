package com.Project.PVZ.models.entities.zombies.behavior.attack;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.game.GameSession;

import java.util.ArrayList;
import java.util.List;

public class SquashSuicideAttack implements AttackBehavior {

    private final Zombie zombie;

    public SquashSuicideAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Tile targetTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());

        if (targetTile != null) {
            List<Plant> plantsHere = targetTile.getPlants();

            if (!plantsHere.isEmpty()) {
                List<Plant> plantsToCrush = new ArrayList<>(plantsHere);

                for (Plant targetPlant : plantsToCrush) {
                    if (targetPlant != null && !targetPlant.isDead()) {
                        targetPlant.takeDamage(Integer.MAX_VALUE);
                        notify("Zombotany: " + zombie.getName() + " crushed " + targetPlant.getName() + "!");
                    }
                }

                zombie.takeDamage(Integer.MAX_VALUE);
                notify("Zombotany: " + zombie.getName() + " destroyed itself!");
            }
        }
    }
}
