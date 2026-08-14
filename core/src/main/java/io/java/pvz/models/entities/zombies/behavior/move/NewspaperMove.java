package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.entities.zombies.behavior.effect.RageEffect;

public class NewspaperMove implements MoveBehavior {
    private final Zombie zombie;
    private final float enragedSpeedMultiplier = 4.0f;
    private final int enragedEatDpsMultiplier = 2;
    private boolean isEnraged;

    public NewspaperMove(Zombie zombie) {
        this.zombie = zombie;
        this.isEnraged = false;
    }

    @Override
    public void execute() {
        if (zombie.getState() == ZombieState.ENRAGING) {
            return;
        }

        if (!isEnraged) {
            checkNewspaperStatus();
        }

        zombie.move();
    }

    private void checkNewspaperStatus() {
        boolean hasNewspaper = false;

        for (Armor armor : zombie.getArmorPieces()) {
            if (armor.getData().getAlias().toLowerCase().contains("newspaper") && !armor.isDestroyed()) {
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
