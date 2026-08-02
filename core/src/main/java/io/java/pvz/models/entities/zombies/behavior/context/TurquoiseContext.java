package io.java.pvz.models.entities.zombies.behavior.context;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class TurquoiseContext implements GameEventListener {
    private final Zombie zombie;
    private boolean isCharging = false;
    private int chargeTicks = 0;
    private int stolenSuns = 0;

    public TurquoiseContext(Zombie zombie) {
        this.zombie = zombie;

        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED, this);
        GameEventMessenger.getInstance().addListener(GameEvent.GAME_OVER, this);
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        boolean isGameOver = (event == GameEvent.GAME_OVER);
        boolean isThisZombieKilled =
                (event == GameEvent.ZOMBIE_KILLED && payload != null && payload.getZombie() == this.zombie);

        if (isGameOver || isThisZombieKilled) {
            if (isThisZombieKilled) {
                int sunToDrop = stolenSuns / 2;
                if (sunToDrop > 0) {
                    GameSession.getInstance().addSun(sunToDrop);
                    GameSession.notify("Turquoise Skull Zombie died and dropped " + sunToDrop + " suns!");
                }
            }

            GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED, this);
            GameEventMessenger.getInstance().removeListener(GameEvent.GAME_OVER, this);
        }
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void startCharging() {
        isCharging = true;
    }

    public void reset() {
        isCharging = false;
        chargeTicks = 0;
    }

    public int getChargeTicks() {
        return chargeTicks;
    }

    public void incrementCharge() {
        chargeTicks++;
    }

    public void addStolenSun(int amount) {
        stolenSuns += amount;
    }
}
