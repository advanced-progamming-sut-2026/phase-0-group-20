package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieType;
import models.entities.zombies.armour.Armor;
import models.entities.zombies.armour.ArmorData;
import models.entities.zombies.armour.ArmorLoader;
import models.game.GameSession;

import java.util.List;

public class KingAttack implements AttackBehavior {
    private final Zombie zombie;

    public KingAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();

        List<Zombie> nearbyZombies = session.getArena().getZombiesInRadius(zombie.getCol(), zombie.getRow(), 2.0);

        for (Zombie target : nearbyZombies) {
            if (target != zombie && !target.isDead()
                    && target.getType() == ZombieType.NORMAL
                    && target.getArmorPieces().isEmpty()) {

                try {
                    ArmorData knightArmorData = ArmorLoader.getInstance().get("ShoulderArmorDefault");
                    target.addArmor(new Armor(knightArmorData));
                    target.setType(ZombieType.DARK_ARMOR);

                    notify(zombie.getName() + " granted knighthood to a zombie in row " + target.getRow() + "!");
                    break;
                } catch (IllegalArgumentException e) {
                    notify("Warning: Knight armor data not found in loader.");
                }
            }
        }
    }
}
