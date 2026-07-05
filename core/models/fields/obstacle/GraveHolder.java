package models.fields.obstacle;

import models.game.GameSession;

public interface GraveHolder {

    GraveStone getGraveStone();

    void removeGrave();

    default void takeDamage(int damage, int row, int col) {
        GraveStone graveStone = getGraveStone();
        if (graveStone == null) return;
        graveStone.takeDamage(damage);

        if (graveStone.getHp() <= 0) {
            System.out.println("Grave destroyed at row: " + row + ", col: " + col);
            GameSession session = GameSession.getInstance();
            if (graveStone.hasSun()) session.addSun(50);
            if (graveStone.hasPlantFood()) session.spawnPlantFood(row, col);

            removeGrave();
        }
    }
}
