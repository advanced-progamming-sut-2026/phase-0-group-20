package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.obstacle.ArcadeMachine;
import io.java.pvz.models.entities.obstacle.Barrel;
import io.java.pvz.models.entities.obstacle.Piano;
import io.java.pvz.models.entities.obstacle.PushableObstacle;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.fields.LawnMower;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.RedLineCapable;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class WorldItemRenderer {
    private final Group effectLayer;
    private final Group mowerLayer;
    private final Group zombieLayer;
    private final Group highlightLayer;
    private final GameFlowController gameFlowController = new GameFlowController();

    private final Map<Projectile, PamAnimatedActor> projectileActors = new HashMap<>();
    private final Map<Projectile, ProjectileType> projectileActorTypes = new HashMap<>();
    private final Map<Sun, PamAnimatedActor> sunActors = new HashMap<>();
    private final Map<LawnMower, PamAnimatedActor> lawnMowerActors = new HashMap<>();
    private final Map<PushableObstacle, PamAnimatedActor> obstacleActors = new HashMap<>();
    private final Map<Brain, Image> brainActors = new HashMap<>();

    private Image redLineActor;
    private final Texture redLineTexture;

    public record ProjectileAnim(String path, String clip) {
    }

    private static final Map<ProjectileType, ProjectileAnim> PROJECTILE_ANIMS = buildProjectileAnimMap();

    public WorldItemRenderer(Group effectLayer, Group mowerLayer, Group zombieLayer, Group highlightLayer) {
        this.effectLayer = effectLayer;
        this.mowerLayer = mowerLayer;
        this.zombieLayer = zombieLayer;
        this.highlightLayer = highlightLayer;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 0f, 0f, 1f);
        pixmap.fill();
        redLineTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void sync(Arena arena) {
        syncProjectiles(arena.getActiveProjectiles());
        syncSuns(arena.getActiveSuns());
        syncLawnMowers(arena.getLawnMowers());
        syncObstacles(arena.getActiveObstacles());
        syncBrains(arena);
        syncRedLine(arena);
    }

    public void clear() {
        projectileActors.clear();
        projectileActorTypes.clear();
        sunActors.clear();
        lawnMowerActors.clear();
        obstacleActors.clear();
        for (Image actor : brainActors.values()) actor.remove();
        brainActors.clear();
        if (redLineActor != null) redLineActor.remove();
    }

    private void syncProjectiles(List<Projectile> liveProjectiles) {
        for (Projectile proj : liveProjectiles) {
            if (!proj.isSpawned()) continue;
            PamAnimatedActor actor = projectileActors.get(proj);
            ProjectileType lastRenderedType = projectileActorTypes.get(proj);

            if (actor == null || lastRenderedType != proj.getType()) {
                if (actor != null) actor.remove();
                actor = spawnProjectile(proj);
                projectileActors.put(proj, actor);
                projectileActorTypes.put(proj, proj.getType());
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
                projectileActorTypes.remove(proj);
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnProjectile(Projectile proj) {
        ProjectileAnim anim = resolveProjectileAnim(proj);

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), anim.clip(), anim.path());

        actor.setSize(30, 30);
        actor.setOrigin(Align.center);
        if (proj.getSpeedX() < 0) actor.setScaleX(-1f);
        effectLayer.addActor(actor);
        return actor;
    }

    private void updateProjectileActor(Projectile proj, PamAnimatedActor actor) {
        float projX = proj.getPosition().getX();
        float projY = proj.getPosition().getY();

        float arcOffsetY = proj.getArcOffsetY();

        actor.setPosition(
            projX - actor.getWidth() / 2f,
            projY - actor.getHeight() / 2f + 40f + arcOffsetY
        );
    }

    public static ProjectileAnim getProjectileAnim(ProjectileType type) {
        return PROJECTILE_ANIMS.getOrDefault(type, PROJECTILE_ANIMS.get(ProjectileType.PEA));
    }

    private ProjectileAnim resolveProjectileAnim(Projectile proj) {
        ProjectileAnim anim = PROJECTILE_ANIMS.get(proj.getType());
        return anim != null ? anim : PROJECTILE_ANIMS.get(ProjectileType.PEA);
    }

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
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getSunAnimation(sun.getType());

        String pamPath2 = anim != null ? anim.path.replace("FULL", "INITIAL") :
            "768/INITIAL/EFFECTS/SUN/SUN.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "animation", pamPath2);

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

        float baseX = sun.getPosition().getX() - actor.getWidth() / 2f;
        float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;

        if (sun.isProducedByPlant()) {
            float offsetX = (float) ((Math.random() - 0.5) * 40.0);
            float targetX = baseX + offsetX;

            actor.setPosition(targetX, targetY + 10f);
            actor.addAction(Actions.sequence(
                Actions.moveTo(targetX, targetY + 60f, 0.35f, Interpolation.sineOut),
                Actions.moveTo(targetX, targetY, 0.35f, Interpolation.bounceOut)
            ));

            sun.getPosition().setX(baseX + offsetX + actor.getWidth() / 2f);

        } else {
            actor.setPosition(baseX, 1180f);
            actor.addAction(Actions.moveTo(baseX, targetY, 4.0f, Interpolation.linear));
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
        if (sun.isBeingAbsorbed()) {
            actor.clearActions();

            AnimationCatalog.EntityAnimation anim = AnimationCatalog.getSunAnimation(sun.getType());
            float transitionDuration = (anim != null && anim.hasClip("transition_red"))
                ? anim.getDuration("transition_red") : 0.33f;

            float currentAbsorbTime = sun.getAbsorbedTicksCounter() / (float) TimeManager.TICKS_PER_SECOND;

            if (currentAbsorbTime < transitionDuration)
                if (!actor.getClip().equals("transition_red")) actor.setClip("transition_red");
                else if (!actor.getClip().equals("red")) actor.setClip("red");
        } else if (!actor.getClip().equals("animation")) actor.setClip("animation");

        if (!sun.isFalling() || sun.isBeingAbsorbed()) {
            float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
            float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;
            actor.setPosition(targetX, targetY);
        }
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

    private PamAnimatedActor spawnLawnMower(LawnMower mower) {
        String mowerKey = resolveMowerKey();

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getMowerAnimation(mowerKey);

        PamAnimatedActor actor;
        if (anim != null) {
            actor = PamAnimatedActor.createEffectAnimated(anim.path, "idle");
        } else {
            actor = new PamAnimatedActor(
                AssetLoader.getInstance().getPlayer(), "idle",
                "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM"
            );
        }

        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        mowerLayer.addActor(actor);

        return actor;
    }

    private void updateLawnMowerActor(LawnMower mower, PamAnimatedActor actor) {
        if (mower.isActivate()) actor.setClip("attack");
        else actor.setClip("idle");


        float offsetX = -20f;
        centerOnPoint(actor, mower.getPosition().getX() + offsetX, mower.getPosition().getY() + 100f);
    }

    private String resolveMowerKey() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getCurrentChapter() == null) return "MOWER_EGYPT";

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

    private void syncObstacles(List<PushableObstacle> obstacles) {
        if (obstacles == null) return;

        Set<PushableObstacle> liveObstacles = new HashSet<>();
        for (PushableObstacle obs : obstacles) {

            if (obs instanceof Barrel) continue;

            if (obs != null && !obs.isDestroyed()) {
                liveObstacles.add(obs);
                PamAnimatedActor actor = obstacleActors.get(obs);
                if (actor == null) {
                    actor = spawnObstacle(obs);
                    obstacleActors.put(obs, actor);
                }
                updateObstacleActor(obs, actor);
            }
        }

        Iterator<Map.Entry<PushableObstacle, PamAnimatedActor>> it = obstacleActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PushableObstacle, PamAnimatedActor> entry = it.next();
            if (!liveObstacles.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnObstacle(PushableObstacle obs) {
        String pamPath;
        String defaultClip = "idle";

        if (obs instanceof ArcadeMachine) {
            pamPath = "768/FULL/EFFECTS/80S_ARCADE_CABINET/80S_ARCADE_CABINET.PAM";
        } else if (obs instanceof Piano) {
            pamPath = "768/FULL/ZOMBIE/PIANO/PIANO.PAM";
            defaultClip = "play";
        } else
            pamPath = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";

        PamAnimatedActor actor = PamAnimatedActor.createEffectAnimated(pamPath, defaultClip);
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        zombieLayer.addActor(actor);
        return actor;
    }

    private void updateObstacleActor(PushableObstacle obs, PamAnimatedActor actor) {
        float offsetY = 0f;

        if (obs instanceof Piano) offsetY = 40f;

        centerOnPoint(actor, obs.getX(), obs.getPosition().getY() + actor.getHeight() / 2f + offsetY);

        if (obs instanceof Piano piano) {
            String targetClip = piano.isPlaying() ? "play" : "idle";
            if (!actor.getClip().equals(targetClip)) actor.setClip(targetClip);
        }
    }

    private void syncBrains(Arena arena) {
        GameSession session = GameSession.getInstance();
        if (!(session != null && session.getCurrentMode() instanceof IZombieLevel)) {
            for (Image actor : brainActors.values()) actor.remove();
            brainActors.clear();
            return;
        }

        Set<Brain> liveBrains = new HashSet<>();

        for (int row = 0; row < arena.getRows(); row++) {
            Brain brain = arena.getBrainInRow(row);
            if (brain == null || brain.isEaten()) continue;

            liveBrains.add(brain);
            Image actor = brainActors.get(brain);
            if (actor == null) {
                Image img = UiFactory.imageFor(AssetLoader.getInstance().getTextures(),
                    "IMAGE_ZOMBIE_POWER_BRAIN_PROJECTILE_POWER_BRAIN_PROJECTILE_112X82");
                img.setScale(1.2f);
                mowerLayer.addActor(img);
                brainActors.put(brain, img);
                actor = img;
            }

            float x = GRID_START_X - (TILE_WIDTH * 0.35f) - 70;
            float y = GRID_START_Y + (row * TILE_HEIGHT) + (TILE_HEIGHT / 2f) - (actor.getHeight() / 2f);
            actor.setPosition(x, y);
        }

        Iterator<Map.Entry<Brain, Image>> it = brainActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Brain, Image> entry = it.next();
            if (!liveBrains.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void syncRedLine(Arena arena) {
        GameSession session = GameSession.getInstance();
        Level level;

        if (session == null) {
            if (redLineActor != null) redLineActor.setVisible(false);
            return;
        }

        level = (Level) session.getCurrentMode();
        if (level instanceof RedLineCapable redLineCapable) {
            if (redLineActor == null) {
                redLineActor = new Image(redLineTexture);
                highlightLayer.addActor(redLineActor);
            }

            float x = GRID_START_X + (redLineCapable.getRedLineCol() * TILE_WIDTH) - 3;
            redLineActor.setSize(6f, arena.getRows() * TILE_HEIGHT);
            redLineActor.setPosition(x, GRID_START_Y);
            redLineActor.setVisible(true);
        } else if (redLineActor != null) redLineActor.setVisible(false);
    }

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }
}
