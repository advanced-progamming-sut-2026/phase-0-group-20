package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.GraveStoneTile;
import models.fields.tiles.Tile;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraveSpawnAttack implements AttackBehavior {
    private final Random random = new Random();
    private final Zombie zombie;

    public GraveSpawnAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();

        List<Tile> availableTiles = new ArrayList<>();

        for (int row = 0; row < session.getArena().getRows(); row++) {
            for (int col = 5; col <= session.getArena().getCols(); col++) {
                Tile tile = session.getArena().getTile(row, col);
                if (tile.getPlants().isEmpty()) {
                    availableTiles.add(tile);
                }
            }
        }

        if (!availableTiles.isEmpty()) {
            Tile targetTile = availableTiles.get(random.nextInt(availableTiles.size()));

            session.getArena().changeTile(targetTile.getRow(), targetTile.getCol(),
                    new GraveStoneTile(targetTile.getRow(), targetTile.getCol()));

            System.out.println("Grave spawned at: " + targetTile.getRow() + ", " + targetTile.getCol());
        }

        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
