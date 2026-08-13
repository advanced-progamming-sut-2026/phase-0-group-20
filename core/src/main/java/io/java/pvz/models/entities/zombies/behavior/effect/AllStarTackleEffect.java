package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.context.AllStarContext;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class AllStarTackleEffect extends Effect {
    private final AllStarContext context;
    private final List<Plant> targetPlants;
    private final Zombie targetHypnoZombie;

    private int ticksCounter = 0;
    private final int totalTicks;
    private final int tackleTick;
    private boolean hasTackled = false;

    public AllStarTackleEffect(Zombie zombie, AllStarContext context, List<Plant> targetPlants) {
        super(zombie, -1);
        this.context = context;
        this.targetPlants = targetPlants;
        this.targetHypnoZombie = null;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float duration = (anim != null && anim.hasClip("tackle")) ? anim.getDuration("tackle") : 1.3f;
        this.totalTicks = (int) (duration * TimeManager.TICKS_PER_SECOND);
        this.tackleTick = (int) (totalTicks * 0.5f);
    }

    public AllStarTackleEffect(Zombie zombie, AllStarContext context, Zombie targetHypnoZombie) {
        super(zombie, -1);
        this.context = context;
        this.targetPlants = null;
        this.targetHypnoZombie = targetHypnoZombie;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float duration = (anim != null && anim.hasClip("tackle")) ? anim.getDuration("tackle") : 1.3f;
        this.totalTicks = (int) (duration * TimeManager.TICKS_PER_SECOND);
        this.tackleTick = (int) (totalTicks * 0.5f);
    }

    @Override
    public void onApply() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.SPECIAL);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        zombie.setState(ZombieState.SPECIAL);
        ticksCounter++;

        if (ticksCounter == tackleTick && !hasTackled) {
            if (targetPlants != null) {
                for (Plant p : targetPlants) {
                    if (!p.isDead()) {
                        p.takeDamage(99999);
                        notify("All-Star Zombie tackled and destroyed " + p.getName() + "!");
                    }
                }
            } else if (targetHypnoZombie != null && !targetHypnoZombie.isDead()) {
                targetHypnoZombie.takeDamage(99999);
                notify("All-Star Zombie tackled a hypnotized zombie!");
            }
            hasTackled = true;
        }

        if (ticksCounter >= totalTicks) {
            finishTackle();
        }
    }

    private void finishTackle() {
        context.setTackled();
        zombie.setState(ZombieState.WALKING);
        zombie.resetSpeed();
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!context.hasTackled() && !zombie.isDead()) {
            finishTackle();
        }
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead() || context.hasTackled();
    }
}
