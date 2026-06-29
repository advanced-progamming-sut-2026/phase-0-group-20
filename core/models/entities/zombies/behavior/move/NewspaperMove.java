package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.entities.zombies.armour.Armor;

public class NewspaperMove implements MoveBehavior {
    private final Zombie zombie;
    private boolean isEnraged;
    private final float enragedSpeedMultiplier = 3.0f; // it can change

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
        zombie.applySpeedMultiplier(enragedSpeedMultiplier);

        // TODO : add new func for increase the rate of DPS in Zombie Class and call it here
    }
}
