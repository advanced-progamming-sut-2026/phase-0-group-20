package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.effect.SheepEffect;
import io.java.pvz.models.entities.plants.effect.PlantEffect;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import javax.sql.rowset.serial.SerialJavaObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WizardSpellEffect extends Effect {
    private enum Phase { IDLE, SPELL }

    private final int spellIntervalTicks;
    private final Random random = new Random();

    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;
    private int spellAnimationTicks;
    private int castFrameTick;
    private boolean hasCasted;

    public WizardSpellEffect(Zombie zombie, int spellIntervalSeconds) {
        super(zombie, -1);
        this.spellIntervalTicks = spellIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.IDLE;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tDur = (anim != null && anim.hasClip("sheep")) ? anim.getDuration("sheep") : 2.3f;

        this.spellAnimationTicks = (int) (tDur * TimeManager.TICKS_PER_SECOND);
        this.castFrameTick = (int) (spellAnimationTicks * 0.5f);
    }

    @Override
    public void onApply() {
        this.currentPhase = Phase.IDLE;
        this.intervalTicksCounter = 0;
        this.phaseTicksCounter = 0;
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        switch (currentPhase) {
            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= spellIntervalTicks) {
                    if (hasValidTarget()) {
                        currentPhase = Phase.SPELL;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        hasCasted = false;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.SPELL);
                        zombie.applySpeedMultiplier(0f);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case SPELL:
                phaseTicksCounter++;

                if (phaseTicksCounter == castFrameTick && !hasCasted) {
                    castSpell();
                    hasCasted = true;
                }

                if (phaseTicksCounter >= spellAnimationTicks) {
                    currentPhase = Phase.IDLE;
                    phaseTicksCounter = 0;

                    if (!zombie.isDead()) {
                        zombie.setState(ZombieState.WALKING);
                        zombie.resetSpeed();
                    }
                }
                break;
        }
    }

    private boolean hasValidTarget() {
        GameSession session = GameSession.getInstance();
        for (Plant p : session.getArena().getActivePlants()) {
            if (!isAlreadyTransformed(p)) return true;
        }
        return false;
    }

    private void castSpell() {
        GameSession session = GameSession.getInstance();
        List<Plant> validTargets = new ArrayList<>();

        for (Plant p : session.getArena().getActivePlants()) {
            if (!isAlreadyTransformed(p)) {
                validTargets.add(p);
            }
        }

        if (!validTargets.isEmpty()) {
            Plant target = validTargets.get(random.nextInt(validTargets.size()));

            target.addEffect(new SheepEffect(zombie));

            GameSession.notify("Wizard transformed " + target.getName() + "!");
        }
    }

    private boolean isAlreadyTransformed(Plant plant) {
        for (PlantEffect effect : plant.getActiveEffects()) {
            if (effect instanceof SheepEffect) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!zombie.isDead()) {
            zombie.resetSpeed();
            zombie.setState(ZombieState.WALKING);
        }
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
