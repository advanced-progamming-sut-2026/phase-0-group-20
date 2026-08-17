package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.entities.zombies.behavior.effect.*;
import io.java.pvz.models.entities.zombies.behavior.move.BarrelRollerMove;
import io.java.pvz.models.entities.zombies.zomboss.MammothFreezingColumn;
import io.java.pvz.models.entities.zombies.zomboss.MammothZomboss;
import io.java.pvz.models.entities.zombies.zomboss.Zomboss;
import io.java.pvz.utils.AnimationCatalog;
import io.java.pvz.utils.PamAnimatedActor;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.TILE_HEIGHT;
import static io.java.pvz.models.enums.PhysicalConstants.TILE_WIDTH;

public class ZombieRenderer {

    private static final String CLIP_IDLE = "idle";
    private static final String CLIP_WALK = "walk";
    private static final float DESPAWN_LINGER_SECONDS = 0.5f;
    private static final float DESPAWN_FADE_SECONDS = 0.25f;

    private final Group zombieLayer;
    private final Map<Zombie, PamAnimatedActor> zombieActors = new HashMap<>();
    private final Map<Zombie, ZombieType> zombieActorTypes = new HashMap<>();

    public ZombieRenderer(Group zombieLayer) {
        this.zombieLayer = zombieLayer;
    }

    public void syncZombies(List<Zombie> liveZombies) {
        for (Zombie zombie : liveZombies) {
            PamAnimatedActor actor = zombieActors.get(zombie);
            ZombieType lastRenderedType = zombieActorTypes.get(zombie);

            if (actor == null || lastRenderedType != zombie.getType()) {
                if (actor != null) {
                    actor.remove();
                }
                actor = spawnZombie(zombie);
                zombieActors.put(zombie, actor);
                zombieActorTypes.put(zombie, zombie.getType());
            }
            if (zombie.getCurrentSpeed() < 0) actor.setScaleX(-1);
            updateZombieActor(zombie, actor);
        }

        Set<Zombie> stillAlive = new HashSet<>(liveZombies);
        Iterator<Map.Entry<Zombie, PamAnimatedActor>> it = zombieActors.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Zombie, PamAnimatedActor> entry = it.next();
            Zombie zombie = entry.getKey();
            PamAnimatedActor actor = entry.getValue();

            if (!stillAlive.contains(zombie)) {
                if (zombie.isBurnedToAsh()) {
                    spawnAshEffect(zombie);
                    actor.remove();
                } else {
                    String deathClip = resolveZombieClip(zombie);
                    actor.setClip(deathClip);

                    float lingerTime = DESPAWN_LINGER_SECONDS;
                    AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie);

                    if (anim != null && anim.hasClip(deathClip)) {
                        lingerTime = anim.getDuration(deathClip);
                    }

                    if (zombie instanceof Zomboss) {
                        lingerTime = Math.max(lingerTime, 4.0f);
                    }

                    despawn(actor, lingerTime - 0.5f);
                }

                zombieActorTypes.remove(zombie);
                it.remove();
            }
        }

        zombieLayer.getChildren().sort((a, b) -> {
            float ay = a.getY();
            float by = b.getY();
            return Float.compare(by, ay);
        });
    }

    private PamAnimatedActor spawnZombie(Zombie zombie) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie);
        String clip = resolveZombieClip(zombie);
        PamAnimatedActor actor = anim != null
            ? PamAnimatedActor.createEffectAnimated(anim.path, clip)
            : PamAnimatedActor.createZombieAnimated(zombie.getType(), clip);

        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);

        actor.setScale(1f, 1f);


        if (zombie.getCurrentSpeed() < 0) actor.setScaleX(-1);
        zombieLayer.addActor(actor);
        return actor;
    }

    private void updateZombieActor(Zombie zombie, PamAnimatedActor actor) {
        actor.setClip(resolveZombieClip(zombie));

        float yOffset = (zombie instanceof Zomboss) ? actor.getHeight() + 40f : actor.getHeight() / 2f + 30f;
        float xOffset = (zombie instanceof Zomboss) ? 40f : 0f;

        centerOnPoint(actor, zombie.getPosition().getX() - xOffset, zombie.getPosition().getY() + yOffset);

        actor.setVisible(zombie.getSpawnEffect() != Zombie.SpawnEffect.SANDSTORM);

        if (zombie.isHypnotized()) {
            actor.setScaleX(-1f);
        } else {
            actor.setScaleX(1f);
        }

        if (zombie.isHypnotized()) {
            actor.setColor(1f, 0.4f, 1f, 1f);
        } else {
            boolean isPoisoned = false;
            boolean isChilled = false;
            boolean isFrozen = false;

            if (zombie.getActiveEffects() != null) {
                for (ZombieEffect effect : zombie.getActiveEffects()) {
                    if (effect instanceof PoisonEffect) isPoisoned = true;
                    if (effect instanceof ChillEffect) isChilled = true;
                    if (effect instanceof FreezeEffect) isFrozen = true;
                }
            }

            if (isFrozen) {
                actor.setColor(0.3f, 0.6f, 1f, 1f);
            } else if (isChilled) {
                actor.setColor(0.7f, 0.9f, 1f, 1f);
            } else if (isPoisoned) {
                actor.setColor(0.6f, 1f, 0.2f, 1f);
            } else {
                actor.setColor(1f, 1f, 1f, 1f);
            }
        }

        if (!zombie.isDead()) {
            updateZombieArmorVisuals(zombie, actor);
        } else {
            actor.setVisibilityMap(null);
        }
    }

    private String resolveZombieClip(Zombie zombie) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());

        if (zombie instanceof Zomboss zomboss) {
            if (zomboss.isDead()) return "die";
            if (zomboss.getState() == ZombieState.INTRO) return "intro";

            return switch (zomboss.getState()) {
                case BOSS_VACUUM_START -> "suction_on";
                case BOSS_VACUUM_LOOP -> "suction_loop";
                case BOSS_VACUUM_END -> "suction_off";
                case BOSS_FIREBOMB_START -> "fire_bomb";
                case BOSS_FIREBOMB_LOOP -> "fire_bomb_loop";
                case BOSS_FIREBOMB_END -> "fire_bomb_end";
                case BOSS_SUMMON_START -> pickClip(anim, "spawn", "zombie_portal_start", "slingshot", "summoning");
                case BOSS_SUMMON_LOOP -> pickClip(anim, "spawn", "zombie_portal_loop", "slingshot", "summoning");
                case BOSS_SUMMON_END -> pickClip(anim, "spawn", "zombie_portal_end", "slingshot", "summoning");
                case BOSS_GLACIER -> {
                    if (zomboss.getAttackBehavior() instanceof MammothFreezingColumn colAttack) {
                        int animIndex = colAttack.getTargetCol() + 1;
                        yield "glacier_column_" + animIndex;
                    }
                    yield "glacier_column_1";
                }
                case BOSS_MISSILE -> (zomboss instanceof MammothZomboss)? "slingshot" : "missile_start";
                case BOSS_DASH -> "walk_forward";
                case BOSS_JUMP -> "jump_start";
                case BOSS_WIND -> "wind_1";
                case BOSS_FIRE_ROW -> "fire_attack";
                case BOSS_SHARK -> "spawn";
                case STUNNED -> pickClip(anim, "idle", "stun_loop", "stun");
                default -> pickClip(anim, "idle", "idle");
            };
        }

        if (zombie.getType() == ZombieType.BARREL_ROLLER) {
            boolean hasBarrel = zombieHasBarrel(zombie);
            if (zombie.isDead()) {
                return hasBarrel ? pickClip(anim, CLIP_WALK, "die") : pickClip(anim, CLIP_WALK, "die2");
            }
            if (zombie.isAttacking()) {
                return hasBarrel ? pickClip(anim, CLIP_WALK, "eat") : pickClip(anim, CLIP_WALK, "eat2");
            }
            if (zombie.getState() == ZombieState.STUNNED) {
                return hasBarrel ? pickClip(anim, CLIP_WALK, "idle") : pickClip(anim, CLIP_WALK, "idle2");
            }
            return hasBarrel ? pickClip(anim, CLIP_WALK, "walk") : pickClip(anim, CLIP_WALK, "walk2");
        }

        if (zombie.isDead()) return pickClip(anim, CLIP_WALK, "die");
        if (zombie.getState() == ZombieState.TOSS) return pickClip(anim, CLIP_IDLE, "toss");
        if (zombie.getState() == ZombieState.INTRO) return pickClip(anim, "idle", "intro");

        if (zombie.getType() == ZombieType.ALL_STAR) {
            if (zombie.getState() == ZombieState.SPECIAL) {
                return pickClip(anim, CLIP_WALK, "tackle");
            }
            if (zombie.getState() == ZombieState.WALKING && zombie.getCurrentSpeed() > zombie.getBaseSpeed() * 1.5f) {
                return pickClip(anim, CLIP_WALK, "run");
            }
        }

        if (zombie.getType() == ZombieType.PIANIST) {
            if (zombie.getState() == ZombieState.SPECIAL) {
                return pickClip(anim, CLIP_WALK, "play");
            }
            return pickClip(anim, CLIP_IDLE, "idle");
        }

        if (zombie.getType() == ZombieType.CRYSTAL_SKULL) {
            if (zombie.getState() == ZombieState.POWER_UP) return pickClip(anim, CLIP_WALK, "power_up");
            if (zombie.getState() == ZombieState.POWER) return pickClip(anim, CLIP_WALK, "power");
            if (zombie.getState() == ZombieState.SPECIAL) return pickClip(anim, CLIP_WALK, "attack");
            if (zombie.getState() == ZombieState.POWER_DOWN) return pickClip(anim, CLIP_WALK, "power_down");
        }

        if (zombie.getState() == ZombieState.SPECIAL) return pickClip(anim, "idle", "special");
        if (zombie.getState() == ZombieState.CAST) return pickClip(anim, CLIP_IDLE, "cast");
        if (zombie.getState() == ZombieState.CAST_LOOP) return pickClip(anim, CLIP_IDLE, "cast_loop");
        if (zombie.getState() == ZombieState.REEL) return pickClip(anim, CLIP_IDLE, "reel");
        if (zombie.getState() == ZombieState.SMASH) return pickClip(anim, "eat", "smash_left");
        if (zombie.getState() == ZombieState.THROW_IMP) return pickClip(anim, "idle", "fire", "cannon_fire");
        if (zombie.getState() == ZombieState.FLYING_IMP) return pickClip(anim, "walk", "fly");
        if (zombie.getState() == ZombieState.LANDING) return pickClip(anim, "idle", "land");
        if (zombie.getState() == ZombieState.FLY_START) return pickClip(anim, CLIP_WALK, "fly_start");
        if (zombie.getState() == ZombieState.FLYING) return pickClip(anim, CLIP_WALK, "fly_loop", "fly");
        if (zombie.getState() == ZombieState.FLY_END) return pickClip(anim, CLIP_WALK, "fly_end", "land");

        if (zombie.getType() == ZombieType.NEWSPAPER) {
            if (zombie.getState() == ZombieState.ENRAGING) {
                return pickClip(anim, CLIP_WALK, "newspaper_defeat");
            }
            boolean isEnraged = zombie.getActiveEffects().stream().anyMatch(e -> e instanceof RageEffect);
            if (isEnraged) {
                if (zombie.isAttacking()) return pickClip(anim, CLIP_WALK, "eat");
                return pickClip(anim, CLIP_WALK, "walk");
            } else {
                if (zombie.isAttacking()) return pickClip(anim, CLIP_WALK, "eat_newspaper");
                return pickClip(anim, CLIP_WALK, "walk_newspaper");
            }
        }

        if (zombie.getType() == ZombieType.TROGLOBITE || zombie.getType() == ZombieType.ARCADE) {
            if (zombie.getState() == ZombieState.PUSH) {
                return pickClip(anim, CLIP_WALK, "push");
            }
        }

        if (zombie.getState() == ZombieState.POWER_UP) return pickClip(anim, CLIP_WALK, "power_up");
        if (zombie.getState() == ZombieState.POWER) return pickClip(anim, CLIP_WALK, "power");
        if (zombie.getState() == ZombieState.POWER_DOWN) return pickClip(anim, CLIP_WALK, "power_down");
        if (zombie.getState() == ZombieState.THROW) return pickClip(anim, CLIP_WALK, "throw");
        if (zombie.getState() == ZombieState.SPIN_UP) return pickClip(anim, CLIP_IDLE, "spinup");
        if (zombie.getState() == ZombieState.SPINNING) return pickClip(anim, CLIP_WALK, "spin_walk", "spin");
        if (zombie.getState() == ZombieState.SPIN_DOWN) return pickClip(anim, CLIP_IDLE, "spindown");
        if (zombie.getState() == ZombieState.SPELL) return pickClip(anim, "idle", "sheep");
        if (zombie.isAttacking()) return pickClip(anim, CLIP_WALK, "eat");
        if (zombie.getState() == ZombieState.STUNNED) return pickClip(anim, CLIP_WALK, "stun_idle", "stun_loop");

        return pickClip(anim, CLIP_IDLE, "walk");
    }

    private String pickClip(AnimationCatalog.EntityAnimation anim, String fallback, String... preferredClips) {
        if (anim != null) {
            for (String clip : preferredClips) {
                if (anim.hasClip(clip)) return clip;
            }
        }
        return fallback;
    }

    private void updateZombieArmorVisuals(Zombie zombie, PamAnimatedActor zombieActor) {
        Map<String, Boolean> visibilityMap = new HashMap<>();

        if (zombie.getArmorPieces() != null) {
            for (Armor armor : zombie.getArmorPieces()) {
                if (!armor.isDestroyed()) {
                    int damageLayer = armor.getDamageLayer();
                    String state = armor.getData().getArmorLayer(damageLayer);

                    if (state != null) {
                        visibilityMap.put(state, true);
                    }

                    String group = armor.getData().getArmorLayerGroup();
                    if (group != null) {
                        visibilityMap.put(group, true);
                    }
                }
            }
        }
        zombieActor.setVisibilityMap(visibilityMap);
    }

    private boolean zombieHasBarrel(Zombie zombie) {
        if (zombie.getMoveBehavior() instanceof BarrelRollerMove moveBehavior) {
            return moveBehavior.getBarrel() != null && !moveBehavior.getBarrel().isDestroyed();
        }
        return false;
    }

    public void spawnAshEffect(Zombie zombie) {
        String pamPath = resolveAshPamPath(zombie.getType());

        PamAnimatedActor ashActor = PamAnimatedActor.createEffectAnimated(pamPath, "animation");

        ashActor.setSize(TILE_WIDTH, TILE_HEIGHT);
        ashActor.setOrigin(Align.center);

        float x = zombie.getPosition().getX() - ashActor.getWidth() / 2f;
        float y = zombie.getPosition().getY() + 45f;

        ashActor.setPosition(x, y);

        if (zombie.getCurrentSpeed() < 0 || zombie.isHypnotized()) {
            ashActor.setScaleX(-1f);
        }

        if (zombie instanceof Zomboss) {
            ashActor.setScale(1.75f, 1.75f);
        }

        zombieLayer.addActor(ashActor);

        ashActor.addAction(Actions.sequence(
            Actions.delay(2.5f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));
    }

    private String resolveAshPamPath(ZombieType type) {
        return switch (type) {
            case GARGANTUAR, ZOMBOSS_EGYPT, ZOMBOSS_FROZEN_CAVES, ZOMBOSS_DARK_AGES, ZOMBOSS_BEACH ->
                "768/INITIAL/EFFECTS/ZOMBIE_GARGANTUAR_ASH/ZOMBIE_GARGANTUAR_ASH.PAM";
            case IMP, IMP_DRAGON -> "768/INITIAL/EFFECTS/ZOMBIE_IMP_ASH/ZOMBIE_IMP_ASH.PAM";
            case KING, ALL_STAR -> "768/INITIAL/EFFECTS/ZOMBIE_BIG_ASH/ZOMBIE_BIG_ASH.PAM";
            case JANE -> "768/FULL/EFFECTS/ZOMBIE_LOSTCITY_JANE_ASH/ZOMBIE_LOSTCITY_JANE_ASH.PAM";
            default -> "768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM";
        };
    }

    public void animateImpFlight(Zombie imp, float startX, float startY) {
        if (imp == null) return;
        PamAnimatedActor actor = zombieActors.get(imp);
        if (actor == null) {
            actor = spawnZombie(imp);
            zombieActors.put(imp, actor);
            zombieActorTypes.put(imp, imp.getType());
        }

        float targetX = imp.getPosition().getX() - actor.getWidth() / 2f;
        float targetY = imp.getPosition().getY() + actor.getHeight() / 2f + 30;

        actor.setPosition(startX, startY + 100f);

        actor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(targetX, targetY, 0.8f, Interpolation.linear),
                Actions.sequence(
                    Actions.moveBy(0, 250f, 0.4f, Interpolation.sineOut),
                    Actions.moveBy(0, -250f, 0.4f, Interpolation.sineIn)
                )
            )
        ));
    }

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }

    private void despawn(PamAnimatedActor actor, float lingerTime) {
        actor.addAction(Actions.sequence(
            Actions.delay(lingerTime),
            Actions.fadeOut(DESPAWN_FADE_SECONDS),
            Actions.removeActor()
        ));
    }

    public void clear() {
        zombieActors.clear();
        zombieActorTypes.clear();
        zombieLayer.clearChildren();
    }
}
