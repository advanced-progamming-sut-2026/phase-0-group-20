package io.java.pvz.views.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.LawnMower;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.utils.AnimationCatalog;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.TILE_HEIGHT;
import static io.java.pvz.models.enums.PhysicalConstants.TILE_WIDTH;

public class BattlefieldRenderer {
    private static final String CLIP_IDLE = "idle";
    private static final String CLIP_WALK = "walk";

    private static final float DESPAWN_LINGER_SECONDS = 0.5f;
    private static final float DESPAWN_FADE_SECONDS = 0.25f;

    private final Map<Plant, PamAnimatedActor> plantActors = new HashMap<>();
    private final Map<Zombie, PamAnimatedActor> zombieActors = new HashMap<>();
    private final Map<Projectile, PamAnimatedActor> projectileActors = new HashMap<>();
    private final Map<Sun, PamAnimatedActor> sunActors = new HashMap<>();
    private final Map<LawnMower, PamAnimatedActor> lawnMowerActors = new HashMap<>();
    private final GameFlowController  gameFlowController = new GameFlowController();
    private final EnvironmentRenderer environmentRenderer;

    private final Group masterGroup = new Group();
    private final Group environmentLayer = new Group();
    private final Group plantLayer = new Group();
    private final Group zombieLayer = new Group();
    private final Group effectLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group highlightLayer = new Group();

    public BattlefieldRenderer() {
        masterGroup.addActor(environmentLayer);
        masterGroup.addActor(highlightLayer);
        masterGroup.addActor(mowerLayer);
        masterGroup.addActor(plantLayer);
        masterGroup.addActor(zombieLayer);
        masterGroup.addActor(effectLayer);

        environmentRenderer = new EnvironmentRenderer(environmentLayer);
    }

    public Group getGroup() {
        return masterGroup;
    }


    public void sync(Arena arena) {
        if (arena == null) return;

        environmentRenderer.sync(arena);
        syncLawnMowers(arena.getLawnMowers());
        syncPlants(arena.getActivePlants());
        syncZombies(arena.getActiveZombies());
        syncProjectiles(arena.getActiveProjectiles());
        syncSuns(arena.getActiveSuns());
    }

    public void clear() {
        environmentRenderer.clear();
        mowerLayer.clearChildren();
        plantLayer.clearChildren();
        zombieLayer.clearChildren();
        effectLayer.clearChildren();

        lawnMowerActors.clear();
        plantActors.clear();
        zombieActors.clear();
        projectileActors.clear();
        sunActors.clear();
    }

    private void syncLawnMowers(LawnMower[] mowers) {
        if (mowers == null) return;

        Set<LawnMower> liveMowers = new HashSet<>();

        for (LawnMower mower : mowers) {
            if (mower != null && !mower.isDestroyed()) {
                liveMowers.add(mower);

                PamAnimatedActor actor = lawnMowerActors.get(mower);
                if (actor == null) {
                    actor = spawnLawnMower(mower);
                    lawnMowerActors.put(mower, actor);
                }
                updateLawnMowerActor(mower, actor);
            }
        }

        Iterator<Map.Entry<LawnMower, PamAnimatedActor>> it = lawnMowerActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LawnMower, PamAnimatedActor> entry = it.next();
            if (!liveMowers.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void syncPlants(List<Plant> livePlants) {
        for (Plant plant : livePlants) {
            PamAnimatedActor actor = plantActors.get(plant);
            if (actor == null) {
                actor = spawnPlant(plant);
                plantActors.put(plant, actor);
            }
            updatePlantActor(plant, actor);
        }

        Set<Plant> stillAlive = new HashSet<>(livePlants);
        Iterator<Map.Entry<Plant, PamAnimatedActor>> it = plantActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Plant, PamAnimatedActor> entry = it.next();
            if (!stillAlive.contains(entry.getKey())) {
                despawn(entry.getValue());
                it.remove();
            }
        }

        plantLayer.getChildren().sort((a, b) -> {
            float ay = a.getY();
            float by = b.getY();
            return Float.compare(by, ay);
        });
    }

    private PamAnimatedActor spawnPlant(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        String clip = resolvePlantClip(plant);

        PamAnimatedActor actor = anim != null
            ? PamAnimatedActor.createEffectAnimated(anim.path, clip)
            : PamAnimatedActor.createPlantAnimated(UiFactory.getAtlasName(plant), clip);

        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        plantLayer.addActor(actor);
        return actor;
    }

    private void updatePlantActor(Plant plant, PamAnimatedActor actor) {
        actor.setClip(resolvePlantClip(plant));
        centerOnPoint(actor, plant.getPosition().getX(), plant.getPosition().getY() + actor.getHeight() / 2);
    }

    private String resolvePlantClip(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim == null) return CLIP_IDLE;
        if (anim.hasClip("idle")) return "idle";
        if (anim.hasClip("loop")) return "loop"; // Empowermint-style plants: intro/loop/outro, no "idle"
        Iterator<String> anyClip = anim.getClipNames().iterator();
        return anyClip.hasNext() ? anyClip.next() : CLIP_IDLE;
    }

    private void syncZombies(List<Zombie> liveZombies) {
        for (Zombie zombie : liveZombies) {
            PamAnimatedActor actor = zombieActors.get(zombie);
            if (actor == null) {
                actor = spawnZombie(zombie);
                zombieActors.put(zombie, actor);
            }
            updateZombieActor(zombie, actor);
        }

        Set<Zombie> stillAlive = new HashSet<>(liveZombies);
        Iterator<Map.Entry<Zombie, PamAnimatedActor>> it = zombieActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Zombie, PamAnimatedActor> entry = it.next();
            if (!stillAlive.contains(entry.getKey())) {
                entry.getValue().setClip(resolveZombieClip(entry.getKey())); // will resolve to "die" if the model already flagged it dead
                despawn(entry.getValue());
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
        zombieLayer.addActor(actor);
        return actor;
    }

    private void updateZombieActor(Zombie zombie, PamAnimatedActor actor) {
        actor.setClip(resolveZombieClip(zombie));
        centerOnPoint(actor, zombie.getPosition().getX(), zombie.getPosition().getY() + actor.getHeight() / 2f + 30);
    }

    private String resolveZombieClip(Zombie zombie) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());

        if (zombie.isDead()) return pickClip(anim, CLIP_WALK, "die");
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

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }

    private void despawn(PamAnimatedActor actor) {
        actor.addAction(Actions.sequence(
            Actions.delay(DESPAWN_LINGER_SECONDS),
            Actions.fadeOut(DESPAWN_FADE_SECONDS),
            Actions.removeActor()
        ));
    }

    private void syncSuns(List<Sun> liveSuns) {
        for (Sun sun : liveSuns) {
            if (sun.isCollected()) continue;

            PamAnimatedActor actor = sunActors.get(sun);
            if (actor == null) {
                actor = spawnSun(sun);
                sunActors.put(sun, actor);
            }
            updateSunActor(sun, actor);
        }

        Set<Sun> stillAlive = new HashSet<>(liveSuns);
        Iterator<Map.Entry<Sun, PamAnimatedActor>> it = sunActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Sun, PamAnimatedActor> entry = it.next();
            Sun sun = entry.getKey();
            if (!stillAlive.contains(sun) || sun.isCollected()) {
                entry.getValue().addAction(Actions.sequence(
                    Actions.fadeOut(0.4f),
                    Actions.removeActor()
                ));
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnSun(Sun sun) {
        String pamPath1 = "768/FULL/EFFECTS/SUN/SUN.PAM"; //add a method to find all sun type animation
        String pamPath2 = "768/INITIAL/EFFECTS/SUN/SUN.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPath1, pamPath2);

        actor.setSize(80, 80);
        actor.setOrigin(Align.center);

        float scale = 1.0f;
        if (sun.getType() != null) {
            switch (sun.getType()) {
                case TINY_SUN -> scale = 0.5f;
                case LARGE_SUN -> scale = 1.3f;
                case SPECIAL_SUN, HUGE_SUN -> scale = 1.6f;
                default -> scale = 1.0f;
            }
        }
        actor.setScale(scale, scale);

        float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
        float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;

        boolean isFromSky = (sun.getType() != null);

        if (isFromSky) {
            actor.setPosition(targetX, 1180f);
            actor.addAction(Actions.moveTo(targetX, targetY, 2.5f, Interpolation.linear));
        } else {
            actor.setPosition(targetX, targetY + 40f);
            actor.addAction(Actions.moveTo(targetX, targetY, 1.0f, Interpolation.bounceOut));
        }

        float finalScale = scale;
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!sun.isCollected() && pointer == -1) {

                    Result result = gameFlowController.collectSun(sun.getCol(), sun.getRow());

                    if (result.isSuccessful()) {
                        actor.addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.scaleTo(finalScale * 1.5f, finalScale * 1.5f, 0.2f),
                                Actions.fadeOut(0.2f)
                            ),
                            Actions.removeActor()
                        ));
                    }
                }
            }
        });

        effectLayer.addActor(actor);
        return actor;
    }

    private void updateSunActor(Sun sun, PamAnimatedActor actor) {
        if (!sun.isFalling()) {
            float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
            float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;
            actor.setPosition(targetX, targetY);
        }    }

    private void syncProjectiles(List<Projectile> liveProjectiles) {
        for (Projectile proj : liveProjectiles) {
            PamAnimatedActor actor = projectileActors.get(proj);
            if (actor == null) {
                actor = spawnProjectile(proj);
                projectileActors.put(proj, actor);
            }
            updateProjectileActor(proj, actor);
        }

        Set<Projectile> stillAlive = new HashSet<>(liveProjectiles);
        Iterator<Map.Entry<Projectile, PamAnimatedActor>> it = projectileActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Projectile, PamAnimatedActor> entry = it.next();
            Projectile proj = entry.getKey();
            if (!stillAlive.contains(proj) || proj.isDestroyed()) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnProjectile(Projectile proj) {
        ProjectileAnim anim = resolveProjectileAnim(proj);

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            anim.clip(), anim.path());

        actor.setSize(30, 30);
        actor.setOrigin(Align.center);
        effectLayer.addActor(actor);
        return actor;
    }

    private void updateProjectileActor(Projectile proj, PamAnimatedActor actor) {
        float projX = proj.getPosition().getX();
        float projY = proj.getPosition().getY();

        actor.setPosition(
            projX - actor.getWidth() / 2f,
            projY - actor.getHeight() / 2f + 40f
        );
    }

    private record ProjectileAnim(String path, String clip) {}

    private static final Map<ProjectileType, ProjectileAnim> PROJECTILE_ANIMS = buildProjectileAnimMap();

    private static Map<ProjectileType, ProjectileAnim> buildProjectileAnimMap() {
        Map<ProjectileType, ProjectileAnim> map = new EnumMap<>(ProjectileType.class);
        map.put(ProjectileType.PEA, new ProjectileAnim(Ids.Projectiles.PEA, "animation"));
        map.put(ProjectileType.ICE_PEA, new ProjectileAnim(Ids.Projectiles.ICE_PEA, "animation"));
        map.put(ProjectileType.ROTOBAGA_SEED, new ProjectileAnim(Ids.Projectiles.ROTOBAGA_SEED, "animation"));
        map.put(ProjectileType.FIRE_PEA, new ProjectileAnim(Ids.Projectiles.FIRE_PEA, "animation"));
        map.put(ProjectileType.GOO_PEA, new ProjectileAnim(Ids.Projectiles.GOO_PEA, "projectile_t1"));
        map.put(ProjectileType.MAGIC_BEAM, new ProjectileAnim(Ids.Projectiles.MAGIC_BEAM, "animation"));
        map.put(ProjectileType.LIGHTNING_CLOUD, new ProjectileAnim(Ids.Projectiles.LIGHTNING_CLOUD, "idle"));
        map.put(ProjectileType.CABBAGE, new ProjectileAnim(Ids.Projectiles.CABBAGE, "animation"));
        map.put(ProjectileType.CORN, new ProjectileAnim(Ids.Projectiles.CORN, "animation"));
        map.put(ProjectileType.BUTTER, new ProjectileAnim(Ids.Projectiles.BUTTER, "animation"));
        map.put(ProjectileType.MELON, new ProjectileAnim(Ids.Projectiles.MELON, "animation"));
        map.put(ProjectileType.WINTER_MELON, new ProjectileAnim(Ids.Projectiles.WINTER_MELON, "animation"));
        map.put(ProjectileType.PEPPER, new ProjectileAnim(Ids.Projectiles.PEPPER, "animation"));
        map.put(ProjectileType.GRAPE, new ProjectileAnim(Ids.Projectiles.GRAPE, "animation_forward"));
        map.put(ProjectileType.FUME, new ProjectileAnim(Ids.Projectiles.FUME, "special"));
        map.put(ProjectileType.SPIKE, new ProjectileAnim(Ids.Projectiles.SPIKE, "idle"));
        map.put(ProjectileType.PLASMA_BALL, new ProjectileAnim(Ids.Projectiles.PLASMA_BALL, "Citron_Citrus_Orb"));
        map.put(ProjectileType.WALLNUT_BOWL, new ProjectileAnim(Ids.Projectiles.WALLNUT_BOWL, "animation"));
        map.put(ProjectileType.EXPLODE_NUT_BOWL, new ProjectileAnim(Ids.Projectiles.EXPLODE_NUT_BOWL, "animation"));
        map.put(ProjectileType.GIANT_NUT_BOWL, new ProjectileAnim(Ids.Projectiles.GIANT_NUT_BOWL, "animation"));
        return map;
    }

    private ProjectileAnim resolveProjectileAnim(Projectile proj) {
        ProjectileAnim anim = PROJECTILE_ANIMS.get(proj.getType());
        return anim != null ? anim : PROJECTILE_ANIMS.get(ProjectileType.PEA);
    }

    private PamAnimatedActor spawnLawnMower(LawnMower mower) {
        String mowerKey = resolveMowerKey();

        AnimationCatalog.EntityAnimation anim =
            AnimationCatalog.getMowerAnimation(mowerKey);

        PamAnimatedActor actor;
        if (anim != null) {
            actor = PamAnimatedActor.createEffectAnimated(anim.path, "idle");
        } else {
            actor = new PamAnimatedActor(
                AssetLoader.getInstance().getPlayer(),
                "idle",
                "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM"
            );
        }

        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        mowerLayer.addActor(actor);

        return actor;
    }

    private void updateLawnMowerActor(LawnMower mower, PamAnimatedActor actor) {
        if (mower.isActivate()) {
            actor.setClip("attack");
        } else {
            actor.setClip("idle");
        }

        float offsetX = -20f;

        centerOnPoint(actor, mower.getPosition().getX() + offsetX, mower.getPosition().getY() +100f);
    }

    private String resolveMowerKey() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getCurrentChapter() == null) {
            return "MOWER_EGYPT";
        }

        SeasonType season = session.getCurrentChapter().getSeasonType();
        if (season == null) return "MOWER_EGYPT";

        return switch (season) {
            case ANCIENT_EGYPT -> "MOWER_EGYPT";
            case DARK_AGES -> "MOWER_DARK";
            case BIG_WAVE_BEACH -> "MOWER_BEACH";
            case FROZEN_CAVES -> "MOWER_ICEAGE";
            default -> "MOWER_WILDWEST";

        };
    }

    public Group getHighlightLayer() {
        return highlightLayer;
    }
}
