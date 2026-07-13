package models.fields;

import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.events.GameEvent;
import models.game.GameSession;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public class LawnMower implements Ticker {
    private final int row;
    private final Arena arena;
    private boolean activate;

    public LawnMower(int row, Arena arena) {
        this.row = row;
        this.arena = arena;
        this.activate = false;
    }

    @Override
    public void onTick(int currentTick) {
        this.activate = false;
        for (Zombie zombie : arena.getActiveZombies()) {
            if (!(zombie.isDead()) && zombie.getRow() == row && zombie.getX() <= 0) {
                if (!activate) {
                    activate = true;
                    GameSession.getInstance().setEvent(GameEvent.LAWNMOWER_TRIGGERED);
                    destroyZombies();
                } else {
                    // we lost the game and it depends on how we implement the Win and Lose Conditions.
                    System.out.println("The zombie ate your brain; LOSER!!!\n");
                    GameSession.getInstance().setZombieBreached(true);
                }
            }
        }
    }

    public void destroyZombies() {
        List<String> killedZombiesNames = new ArrayList<>();

        for (Zombie z : arena.getActiveZombies()) {
            if (!z.isDead() && z.getRow() == this.row) {
                z.takeDamage(10000, null);
                killedZombiesNames.add(z.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("The lawn mower in the row " + this.row + 1 + "is triggered and killed these zombies:\n");
        for (String zombieName : killedZombiesNames) {
            sb.append(zombieName + "\n");
        }

        sb.deleteCharAt(sb.length() - 1);

        System.out.println(sb.toString()); // not done
    }

    public boolean isActivate() {
        return activate;
    }
}
