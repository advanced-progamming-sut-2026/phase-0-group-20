package com.Project.PVZ.models.entities.zombies.behavior.move;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.armour.Armor;
import com.Project.PVZ.models.entities.zombies.behavior.effect.RageEffect;

public class NewspaperMove implements MoveBehavior {
    private final Zombie zombie;
    private final float enragedSpeedMultiplier = 3.0f;
    private final int enragedEatDpsMultiplier = 2;// it can change
    private boolean isEnraged;

    public NewspaperMove(Zombie zombie) {
        this.zombie = zombie;
        this.isEnraged = false;
    }


    @Override
    public void execute() {
        if (!isEnraged) {
            checkNewspaperStatus();
        }

        zombie.moveForward();
    }

    private void checkNewspaperStatus() {
        boolean hasNewspaper = false;

        for (Armor armor : zombie.getArmorPieces()) {
            if (armor.getData().getArmorType().equalsIgnoreCase("Newspaper") && !armor.isDestroyed()) {
                hasNewspaper = true;
                break;
            }
        }

        if (!hasNewspaper) {
            enrage();
        }
    }

    private void enrage() {
        isEnraged = true;
        zombie.addEffect(new RageEffect(zombie, enragedSpeedMultiplier, enragedEatDpsMultiplier));
    }
}
