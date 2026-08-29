package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.entities.zombies.armour.ArmorData;
import io.java.pvz.models.entities.zombies.armour.ArmorLoader;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class KingKnightEffect extends Effect {
    private enum Phase { INTRO, IDLE, SPECIAL }

    private final int checkIntervalTicks;
    private Phase currentPhase;
    private int intervalTicksCounter;
    private int phaseTicksCounter;

    private int introTicks;
    private int specialAnimationTicks;
    private int knightFrameTick;
    private boolean hasKnighted;

    private Zombie targetZombie;

    public KingKnightEffect(Zombie zombie, int checkIntervalSeconds) {
        super(zombie, -1);
        this.checkIntervalTicks = checkIntervalSeconds * TimeManager.TICKS_PER_SECOND;
        this.currentPhase = Phase.INTRO;

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());
        float tIntro = (anim != null && anim.hasClip("intro")) ? anim.getDuration("intro") : 3.2333f;
        float tSpecial = (anim != null && anim.hasClip("special")) ? anim.getDuration("special") : 4.0f;

        this.introTicks = (int) (tIntro * TimeManager.TICKS_PER_SECOND);
        this.specialAnimationTicks = (int) (tSpecial * TimeManager.TICKS_PER_SECOND);
        this.knightFrameTick = (int) (specialAnimationTicks * 0.5f);
    }

    @Override
    public void onApply() {
        this.currentPhase = Phase.INTRO;
        this.phaseTicksCounter = 0;
        this.intervalTicksCounter = 0;
        this.targetZombie = null;

        zombie.setBaseSpeed(0f);
        zombie.setCurrentSpeed(0f);
        zombie.setState(ZombieState.INTRO);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        switch (currentPhase) {
            case INTRO:
                phaseTicksCounter++;
                if (phaseTicksCounter >= introTicks) {
                    resetToIdle();
                }
                break;

            case IDLE:
                intervalTicksCounter++;
                if (intervalTicksCounter >= checkIntervalTicks) {
                    targetZombie = findTargetZombie();
                    if (targetZombie != null) {
                        currentPhase = Phase.SPECIAL;
                        phaseTicksCounter = 0;
                        intervalTicksCounter = 0;
                        hasKnighted = false;

                        zombie.setAttacking(false);
                        zombie.setState(ZombieState.SPECIAL);
                    } else {
                        intervalTicksCounter = 0;
                    }
                }
                break;

            case SPECIAL:
                phaseTicksCounter++;

                if (phaseTicksCounter == knightFrameTick && !hasKnighted) {
                    knightZombie();
                    hasKnighted = true;
                }

                if (phaseTicksCounter >= specialAnimationTicks) {
                    resetToIdle();
                }
                break;
        }
    }

    private void resetToIdle() {
        currentPhase = Phase.IDLE;
        phaseTicksCounter = 0;
        targetZombie = null;
        if (!zombie.isDead()) {
            zombie.setState(ZombieState.WALKING);
        }
    }

    private Zombie findTargetZombie() {
        GameSession session = GameSession.getInstance();
        List<Zombie> nearbyZombies = session.getArena().getActiveZombies();

        for (Zombie target : nearbyZombies) {
            if (target != zombie && !target.isDead()
                && target.getType() == ZombieType.NORMAL
                && target.getArmorPieces().isEmpty()) {
                return target;
            }
        }

        for (Zombie target : nearbyZombies) {
            if (target != zombie && !target.isDead()
                && target.getType() == ZombieType.DARK_ARMOR
                && !hasCrown(target)) {
                return target;
            }
        }

        return null;
    }

    private boolean hasCrown(Zombie target) {
        for (Armor armor : target.getArmorPieces()) {
            if (armor.getData() != null && armor.getData().getAlias() != null) {
                String alias = armor.getData().getAlias().toLowerCase();
                if (alias.contains("crown")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void knightZombie() {
        if (targetZombie == null || targetZombie.isDead()) return;

        if (targetZombie.getType() == ZombieType.NORMAL && targetZombie.getArmorPieces().isEmpty()) {
            try {
                ArmorData shoulderArmorData = ArmorLoader.getInstance().get("ShoulderArmorDefault");
                targetZombie.addArmor(new Armor(shoulderArmorData));
                targetZombie.setType(ZombieType.DARK_ARMOR);

                notify(zombie.getName() + " granted shoulder armor to a zombie in row "
                    + (targetZombie.getRow() + 1) + "!");
            } catch (Exception e) {
                notify("Warning: Shoulder armor data not found in loader.");
            }
        }

        else if (targetZombie.getType() == ZombieType.DARK_ARMOR && !hasCrown(targetZombie)) {
            try {
                ArmorData crownArmorData = ArmorLoader.getInstance().get("CrownDefault");
                targetZombie.addArmor(new Armor(crownArmorData));

                notify(zombie.getName() + " granted a crown to a knight in row " + (targetZombie.getRow() + 1) + "!");
            } catch (Exception e) {
                notify("Warning: Crown armor data not found in loader.");
            }
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        resetToIdle();
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return zombie.isDead();
    }
}
