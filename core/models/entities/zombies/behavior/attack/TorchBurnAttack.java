package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;

public class TorchBurnAttack implements AttackBehavior {
    private final Zombie zombie;
    private boolean isTorchLit;

    public TorchBurnAttack(Zombie zombie) {
        this.zombie = zombie;
        this.isTorchLit = true;
    }

    @Override
    public void execute() {
        if (!isTorchLit) {
            int damagePerTick = zombie.getEatDps() / 10;
            // normal damage
            return;
        }

        // TODO : if isTorchLit == true -> execute logic for burning plant

    }

    public void extinguishTorch() {
        this.isTorchLit = false;
    }
}
