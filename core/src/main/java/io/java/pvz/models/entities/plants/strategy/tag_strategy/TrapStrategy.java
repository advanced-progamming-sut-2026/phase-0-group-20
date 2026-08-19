package io.java.pvz.models.entities.plants.strategy.tag_strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.ChillEffect;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class TrapStrategy implements IPlantStrategy {
    private int startTick = -1;
    private boolean isArmed = false;
    private boolean initialized = false;
    private int armingTimeTicks = 0;

    private int extraSmashCharges = 0;
    private int smashCount = 0;

    private int extraGrabTargets = 0;
    private float freezeDurationBonus = 0f;

    private int squashState = 0; // 0=Idle, 1=SizeUp, 2=JumpParabolic
    private Zombie lockedTarget = null;
    private boolean jumpRight = true;
    private float originalPlantX = 0;
    private float originalPlantY = 0;
    private float targetZombieX = 0;
    private int elapsedJumpTicks = 0;
    private int totalJumpDurationTicks = 0;
    private static final float ARC_HEIGHT_TILES = 1.5f;

    @Override
    public void execute(Plant context, int currentTick) {
        String name = context.getName();

        if (!handleArming(context, currentTick, name)) {
            return;
        }

        if (name.equals("Squash") && squashState == 2) {
            handleSquashParabolicLoop(context);
            return;
        }

        if (name.equals("Squash") && squashState == 1) {
            handleSquashAttackSequence(context);
            return;
        }

        int plantRow = context.getPlacedTile().getRow();
        double plantCol = context.getPlacedTile().getCol();

        List<Zombie> targets = findTargets(name, plantRow, plantCol);

        if (!targets.isEmpty()) {

            if (name.equals("Squash")) {
                lockedTarget = targets.getFirst();
                double zColFloat = (lockedTarget.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH;
                jumpRight = zColFloat >= plantCol;

                originalPlantX = context.getPosition().getX();
                originalPlantY = context.getPosition().getY();
                targetZombieX = lockedTarget.getPosition().getX();

                float durationSec = (0.8f + 0.8f);
                totalJumpDurationTicks = (int)(durationSec * TimeManager.TICKS_PER_SECOND);
                elapsedJumpTicks = 0;

                context.triggerAction("size_up");
                squashState = 1;
                return;
            }

            notify("💥 " + name + " TRAP TRIGGERED!");
            int baseDamage = context.getDamage() > 0 ? context.getDamage() : 1800;

            boolean shouldDie = executeTrapEffect(
                name, context, targets, baseDamage, currentTick, plantCol, plantRow
            );

            if (shouldDie) {
                context.takeDamage(context.getCurrentHp());
            }
        }
    }

    private void handleSquashAttackSequence(Plant context) {
        if (context.getCurrentAction() != null) return;

        context.triggerAction(jumpRight ? "jump_up_right" : "jump_up_left");
        squashState = 2;
    }

    private void handleSquashParabolicLoop(Plant context) {
        elapsedJumpTicks++;
        float t = Math.min(1.0f, (float) elapsedJumpTicks / totalJumpDurationTicks);

        if (lockedTarget != null && !lockedTarget.isDead()) {
            targetZombieX = lockedTarget.getPosition().getX();
        }

        context.getPosition().setX(originalPlantX + (targetZombieX - originalPlantX) * t);

        float arcHeightPixels = ARC_HEIGHT_TILES * PhysicalConstants.TILE_HEIGHT;
        float currentYOffset = 4 * arcHeightPixels * t * (1 - t);
        context.getPosition().setY(originalPlantY + currentYOffset);

        float elapsedSec = (float) elapsedJumpTicks / TimeManager.TICKS_PER_SECOND;
        String currentAction = context.getCurrentAction();

        if (elapsedSec >= 0.8f && (currentAction == null || !currentAction.contains("down"))) {
            context.triggerAction(jumpRight ? "jump_down_right" : "jump_down_left");
        }

        if (t >= 1.0f) {
            handleSquashCollision(context);
        }
    }

    private void handleSquashCollision(Plant context) {
        int baseDamage = context.getDamage() > 0 ? context.getDamage() : 1800;
        double collisionRadiusX = PhysicalConstants.TILE_WIDTH * 0.8;

        if (lockedTarget != null && !lockedTarget.isDead()) {
            lockedTarget.takeDamage(baseDamage);
            if (lockedTarget.isDead()) context.onZombieDeath(lockedTarget);
        }

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(context.getPlacedTile().getRow())) {
            if (z.isDead()) continue;

            double distCartesian = Math.abs(z.getPosition().getX() - context.getPosition().getX());

            if (distCartesian <= collisionRadiusX) {
                z.takeDamage(baseDamage);
                if (z.isDead()) context.onZombieDeath(z);
            }
        }

        notify("💥 Squash crushed zombies on their head!");

        context.getPosition().setPosition(originalPlantX, originalPlantY);

        smashCount++;
        int totalAllowedSmashes = 1 + extraSmashCharges;
        if (smashCount < totalAllowedSmashes) {
            squashState = 0;
            lockedTarget = null;
        } else {
            context.takeDamage(context.getCurrentHp());
        }
    }

    private boolean handleArming(Plant context, int currentTick, String name) {
        if (!initialized) {
            armingTimeTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
            if (armingTimeTicks <= 0) {
                isArmed = true;
            }
            initialized = true;
        }

        if (startTick == -1) startTick = currentTick;

        if (!isArmed) {
            if ((currentTick - startTick) >= armingTimeTicks) {
                isArmed = true;
                if (name.contains("Potato Mine")) {
                    context.triggerAction("recover");
                }
                notify("🟢 " + name + " is now armed and ready!");
            } else {
                return false;
            }
        }
        return true;
    }

    private List<Zombie> findTargets(String name, int plantRow, double plantCol) {
        List<Zombie> targets = new ArrayList<>();
        double detectionRadius = name.equals("Squash") ? 2.0 : 0.5;
        int maxTargetsAllowed = name.equals("Tangle Kelp") ? (1 + extraGrabTargets) : 1;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;

            double zColFloat = (z.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH ;
            double dist = Math.abs(zColFloat - plantCol);

            if (dist <= detectionRadius) {
                targets.add(z);
                if (targets.size() >= maxTargetsAllowed) {
                    break;
                }
            }
        }
        return targets;
    }

    private boolean executeTrapEffect(String name, Plant context, List<Zombie> targets,
                                      int baseDamage, int currentTick, double plantCol, int plantRow) {
        boolean shouldDie = true;

        switch (name) {
            case "Potato Mine", "Primal Potato Mine" -> {
                context.triggerAction("attack");

                String explosionMsg = name.equals("Primal Potato Mine") ? "PRIMAL_POTATOMINE_EXPLODE" : "POTATOMINE_EXPLODE";
                GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                    new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                        .message(explosionMsg)
                        .coordinate(plantRow, (int) plantCol)
                        .build()
                );

                if (name.equals("Primal Potato Mine")) {
                    List<Zombie> aoeTargets = GameSession.getInstance().getArena()
                        .getZombiesInRadius((int) plantCol, plantRow, 1.5);
                    for (Zombie z : aoeTargets) {
                        if (!z.isDead()) {
                            z.takeDamage(Math.max(baseDamage, 2400));
                            if (z.isDead()) context.onZombieDeath(z);
                        }
                    }
                } else {
                    Zombie pmTarget = targets.getFirst();
                    pmTarget.takeDamage(baseDamage);
                    if (pmTarget.isDead()) context.onZombieDeath(pmTarget);
                }
            }
            case "Tangle Kelp" -> {
                context.triggerAction("attack");
                for (Zombie z : targets) {
                    z.takeDamage(9999);
                    if (z.isDead()) context.onZombieDeath(z);
                    notify("🌊 Tangle Kelp pulled " + z.getName() + " underwater!");
                }
            }
            case "Iceberg Lettuce" -> {
                context.triggerAction("attack");
                Zombie iceTarget = targets.getFirst();
                iceTarget.addEffect(new ChillEffect(iceTarget, (int) (600 + freezeDurationBonus)));
                notify("❄️ Iceberg Lettuce completely froze " + iceTarget.getName() + "!");
            }
        }

        return shouldDie;
    }

    public void setArmingTimeTicks(int armingTime) { this.armingTimeTicks = armingTime; }
    public int getArmingTimeTicks() { return armingTimeTicks; }
    public void setArmed(boolean armed) { this.isArmed = armed; }
    public void increaseFreezeDuration(float value) { }
    public boolean isArmed() { return isArmed; }
    public void increaseSmashCharges(int amount) { this.extraSmashCharges += amount; }
    public void increaseMaxTargets(int amount) { this.extraGrabTargets += amount; }
}
