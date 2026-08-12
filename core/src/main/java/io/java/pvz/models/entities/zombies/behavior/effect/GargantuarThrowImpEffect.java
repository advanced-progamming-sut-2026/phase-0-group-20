package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieFactory;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

public class GargantuarThrowImpEffect extends Effect {
    private int ticksCounter = 0;
    private final int totalTicks = (int) (0.9667f * TimeManager.TICKS_PER_SECOND);
    private final int throwTick = (int) (totalTicks * 0.5f);
    private boolean hasThrown = false;
    private static final int IMP_LANDING_COLUMN = 2;

    public GargantuarThrowImpEffect(Zombie zombie) {
        super(zombie, -1);
    }

    @Override
    public void onApply() {
        zombie.setState(ZombieState.THROW_IMP);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;
        ticksCounter++;

        if (ticksCounter == throwTick && !hasThrown) {
            throwImp();
            hasThrown = true;
        }

        if (ticksCounter >= totalTicks) {
            zombie.setState(ZombieState.WALKING);
            zombie.resetSpeed();
            zombie.getActiveEffects().remove(this);
        }
    }

    private void throwImp() {
        ZombieType impType = (zombie.getType() == ZombieType.GARGANTUAR) ? ZombieType.IMP_DRAGON : ZombieType.IMP;
        Zombie imp = ZombieFactory.create(impType, zombie.getRow());
        imp.setCol(IMP_LANDING_COLUMN);

        imp.addEffect(new ImpFlightEffect(imp));

        GameSession session = GameSession.getInstance();
        session.getTimeManager().registerNewTicker(imp);
        session.getArena().addZombie(imp);

        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("IMP_THROWN")
                .zombie(imp)
                .pixelCoordinate(zombie.getPosition().getX(), zombie.getPosition().getY())
                .build());
    }

    @Override
    public float getRemainingSeconds() { return 0f; }
    @Override
    public void onRemove() { }
    @Override
    public boolean isFinished() { return zombie.isDead(); }
}
