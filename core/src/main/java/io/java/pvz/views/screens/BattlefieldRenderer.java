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
import io.java.pvz.models.Position;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.DigestionStrategy;
import io.java.pvz.models.entities.plants.strategy.category_strategy.SunProductionStrategy;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.TrapStrategy;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.fields.LawnMower;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.RedLineCapable;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.speciallevels.DeadLine;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.BowlingLevel;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class BattlefieldRenderer implements GameEventListener {
    private static final String CLIP_IDLE = "idle";
    private static final String CLIP_WALK = "walk";

    private static final float DESPAWN_LINGER_SECONDS = 0.5f;
    private static final float DESPAWN_FADE_SECONDS = 0.25f;

    private Image redLineActor;
    private Texture redLineTexture;

    private static final float HIT_SPLASH_SIZE = 70f;

    private record HitAnim(String path, String clip, float duration) {
    }

    private static final Map<ProjectileType, HitAnim> HIT_ANIMS = buildHitAnimMap();

    private final Map<Plant, PamAnimatedActor> plantActors = new HashMap<>();
    private final Map<Zombie, PamAnimatedActor> zombieActors = new HashMap<>();
    private final Map<Projectile, PamAnimatedActor> projectileActors = new HashMap<>();
    private final Map<Projectile, ProjectileType> projectileActorTypes = new HashMap<>();
    private final Map<Sun, PamAnimatedActor> sunActors = new HashMap<>();
    private final Map<LawnMower, PamAnimatedActor> lawnMowerActors = new HashMap<>();
    private final GameFlowController gameFlowController = new GameFlowController();
    private final EnvironmentRenderer environmentRenderer;
    private final Map<Brain, Image> brainActors = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantChillOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantFreezeOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantOctopusOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantSheepOverlays = new HashMap<>();
    private final Map<Zombie, ZombieType> zombieActorTypes = new HashMap<>();

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

        GameEventMessenger.getInstance().addListener(GameEvent.PROJECTILE_HIT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.SPAWN_EFFECT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.NOTIFY, this);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 0f, 0f, 1f);
        pixmap.fill();
        redLineTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public Group getGroup() {
        return masterGroup;
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.PROJECTILE_HIT) {
            spawnHitSplash(payload);
        } else if (event == GameEvent.SPAWN_EFFECT) {
            System.out.println("Effect Received! Type: " + payload.getMessage());

            if ("BONE_HIT".equals(payload.getMessage())) {
                spawnBoneHitEffect(payload.getCol(), payload.getRow());
            } else if ("HUNTER_SNOWBALL_HIT".equals(payload.getMessage())) {
                spawnHunterIceHitEffect(payload.getCol(), payload.getRow());
            } else if ("UPDATE_ICE_OVERLAY".equals(payload.getMessage())) {
                updatePlantIceOverlay(payload.getPlant(), payload.getAmount());
            } else if ("REMOVE_ICE_OVERLAY".equals(payload.getMessage())) {
                removePlantIceOverlay(payload.getPlant());
            } else if ("ICE_BLOCK_DAMAGE".equals(payload.getMessage())) {
                spawnIceBlockDamageEffect(payload.getCol(), payload.getRow());
            } else if ("UPDATE_ICE_CRACKS".equals(payload.getMessage())) {
                updateIceCracks(payload.getPlant(), payload.getAmount());
            } else if ("OCTOPUS_LAND".equals(payload.getMessage())) {
                spawnOctopusOnPlant(payload.getPlant());
            } else if ("OCTOPUS_DIE".equals(payload.getMessage())) {
                killOctopusOnPlant(payload.getPlant());
            }
        } else if (event == GameEvent.NOTIFY && payload.getMessage() != null) {
            String msg = String.valueOf(payload.getMessage());

            if ("DEFLECT_PROJECTILE".equals(msg)) {
                spawnDeflectedProjectileVisual(payload.getZombie(), payload.getPlant(), payload.getProjectileType());
            } else if ("SHEEP_APPLY".equals(msg)) {
                spawnSheepOnPlant(payload.getPlant());
            } else if ("SHEEP_REMOVE".equals(msg)) {
                removeSheepFromPlant(payload.getPlant());
            } else if ("IMP_THROWN".equals(msg)) {
                animateImpFlight(payload.getZombie(), payload.getPixelX(), payload.getPixelY());
            }
        }
    }

    private void animateImpFlight(Zombie imp, float startX, float startY) {
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

    private void spawnSheepOnPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        if (plantActor != null) {
            plantActor.setVisible(false);
        }

        String pamPath = "768/FULL/EFFECTS/DARK_WIZARD_SHEEPENING/DARK_WIZARD_SHEEPENING.PAM";
        PamAnimatedActor sheepActor = plantSheepOverlays.get(plant);

        if (sheepActor == null) {
            sheepActor = PamAnimatedActor.createEffectAnimated(pamPath, "animation");
            sheepActor.setSize(TILE_WIDTH, TILE_HEIGHT);
            sheepActor.setOrigin(Align.center);
            plantLayer.addActor(sheepActor);
            plantSheepOverlays.put(plant, sheepActor);
        }

        if (plantActor != null) {
            sheepActor.setPosition(plantActor.getX(), plantActor.getY() + 15f);
        }

        PamAnimatedActor finalSheep = sheepActor;
        sheepActor.addAction(Actions.sequence(
            Actions.delay(1.7f),
            Actions.run(() -> finalSheep.setClip("idle"))
        ));
    }

    private void removeSheepFromPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor sheepActor = plantSheepOverlays.remove(plant);
        if (sheepActor != null) {
            sheepActor.clearActions();
            sheepActor.setClip("animation2");

            sheepActor.addAction(Actions.sequence(
                Actions.delay(1.1f),
                Actions.removeActor()
            ));
        }

        PamAnimatedActor plantActor = plantActors.get(plant);
        if (plantActor != null) {
            plantActor.addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.visible(true)
            ));
        }
    }

    private void spawnDeflectedProjectileVisual(Zombie zombie, Plant plant, ProjectileType type) {
        if (zombie == null || plant == null || type == null) return;

        ProjectileAnim anim = PROJECTILE_ANIMS.getOrDefault(type, PROJECTILE_ANIMS.get(ProjectileType.PEA));

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), anim.clip(), anim.path());
        actor.setSize(30, 30);
        actor.setOrigin(Align.center);
        actor.setScaleX(-1f);

        float startX = zombie.getPosition().getX() - 20f;
        float startY = zombie.getPosition().getY() + 40f;

        float targetX = plant.getPosition().getX();
        float targetY = plant.getPosition().getY() + 40f;

        actor.setPosition(startX, startY);
        effectLayer.addActor(actor);

        float travelTime = 0.5f;

        actor.addAction(Actions.sequence(
            Actions.moveTo(targetX, targetY, travelTime, Interpolation.linear),
            Actions.run(() -> {
                GameEventPayload hitPayload = new GameEventPayload.Builder(GameEvent.PROJECTILE_HIT)
                    .projectileType(type)
                    .pixelCoordinate(targetX, targetY)
                    .build();
                spawnHitSplash(hitPayload);
            }),
            Actions.removeActor()
        ));
    }

    private void updatePlantIceOverlay(Plant plant, int stacks) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        float targetX = plantActor != null ? plantActor.getX() : 0;
        float targetY = plantActor != null ? plantActor.getY() : 0;

        if (stacks == 1 || stacks == 2) {
            String clipName = stacks == 1 ? "chill_stage1" : "chill_stage2";
            PamAnimatedActor chillActor = plantChillOverlays.get(plant);

            if (chillActor == null) {
                chillActor = PamAnimatedActor.createEffectAnimated(
                    "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM", clipName);
                chillActor.setSize(TILE_WIDTH, TILE_HEIGHT);
                chillActor.setOrigin(Align.center);
                plantLayer.addActor(chillActor);
                plantChillOverlays.put(plant, chillActor);
            } else {
                chillActor.setClip(clipName);
            }
            chillActor.setPosition(targetX, targetY);

        } else if (stacks >= 3) {
            PamAnimatedActor chillActor = plantChillOverlays.remove(plant);
            if (chillActor != null) chillActor.remove();

            PamAnimatedActor freezeActor = plantFreezeOverlays.get(plant);
            if (freezeActor == null) {
                freezeActor = PamAnimatedActor.createEffectAnimated(
                    "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM", "freeze_idle");
                freezeActor.setSize(TILE_WIDTH, TILE_HEIGHT);
                freezeActor.setOrigin(Align.center);
                plantLayer.addActor(freezeActor);
                plantFreezeOverlays.put(plant, freezeActor);
            }
            freezeActor.setPosition(targetX, targetY);
        }
    }

    private void spawnOctopusOnPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        float targetX = plantActor != null ? plantActor.getX() : 0;
        float targetY = plantActor != null ? plantActor.getY() : 0;

        String pamPath = "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";

        PamAnimatedActor octopusActor = plantOctopusOverlays.get(plant);
        if (octopusActor == null) {
            octopusActor = PamAnimatedActor.createEffectAnimated(pamPath, "animation");
            octopusActor.setSize(TILE_WIDTH, TILE_HEIGHT);
            octopusActor.setOrigin(Align.center);
            plantLayer.addActor(octopusActor);
            plantOctopusOverlays.put(plant, octopusActor);
        }

        float startX = targetX + 400f;
        float startY = targetY + 200f;

        octopusActor.setPosition(startX, startY);

        float flyTime = 0.8f;

        PamAnimatedActor finalOctopusActor = octopusActor;
        octopusActor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(targetX, targetY, flyTime, Interpolation.linear),
                Actions.moveBy(0, -200f, flyTime, Interpolation.pow2In)
            ),
            Actions.run(() -> finalOctopusActor.setClip("animation2")),
            Actions.delay(0.9f),

            Actions.run(() -> finalOctopusActor.setClip("animation3"))
        ));
    }

    private void killOctopusOnPlant(Plant plant) {
        if (plant == null) return;
        PamAnimatedActor octopusActor = plantOctopusOverlays.remove(plant);

        if (octopusActor != null) {
            octopusActor.clearActions();
            octopusActor.setClip("die");

            octopusActor.addAction(Actions.sequence(
                Actions.delay(2.0f),
                Actions.removeActor()
            ));
        }
    }

    private void updateIceCracks(Plant plant, int remainingHp) {
        PamAnimatedActor freezeActor = plantFreezeOverlays.get(plant);
        if (freezeActor == null) return;

        Map<String, Boolean> visMap = new HashMap<>();

        if (remainingHp <= 200) {
            visMap.put("damage2", true);
            visMap.put("damage1", false);
            visMap.put("undamaged", false);
        } else if (remainingHp <= 400) {
            visMap.put("damage1", true);
            visMap.put("damage2", false);
            visMap.put("undamaged", false);
        } else {
            visMap.put("undamaged", true);
            visMap.put("damage1", false);
            visMap.put("damage2", false);
        }

        freezeActor.setVisibilityMap(visMap);
    }

    private void removePlantIceOverlay(Plant plant) {
        if (plant == null) return;
        PamAnimatedActor chill = plantChillOverlays.remove(plant);
        if (chill != null) chill.remove();

        PamAnimatedActor freeze = plantFreezeOverlays.remove(plant);
        if (freeze != null) freeze.remove();
    }

    private void spawnIceBlockDamageEffect(int col, int row) {
        String pamPath1 = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PARTICLES/FROSTBITE_ICE_BLOCK_PARTICLES.PAM";
        String pamPath2 = "768/INITIAL/EFFECTS/FROSTBITE_ICE_BLOCK_PARTICLES/FROSTBITE_ICE_BLOCK_PARTICLES.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPath1, pamPath2);

        actor.setSize(80, 80);
        actor.setOrigin(Align.center);

        io.java.pvz.models.Position pos = new io.java.pvz.models.Position(col, row);
        float x = pos.getX() - (actor.getWidth() / 2f);
        float y = pos.getY() - (actor.getHeight() / 2f) + 15f;

        actor.setPosition(x, y);
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(1.0f),
            Actions.removeActor()
        ));
    }

    private void spawnBoneHitEffect(int col, int row) {
        String pamPath = "768/INITIAL/EFFECTS/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "animation", pamPath);

        actor.setSize(100, 100);
        actor.setOrigin(Align.center);

        Position pos = new Position(col, row);

        float x = pos.getX() - (actor.getWidth() / 2f);
        float y = pos.getY() - (actor.getHeight() / 2f) + 40;

        actor.setPosition(x, y);
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(1.5f),
            Actions.removeActor()
        ));
    }

    private void spawnHunterIceHitEffect(int col, int row) {
        String pamPath1 = "768/FULL/EFFECTS/ZOMBIE_HUNTER_SNOWBALL_SPLAT/ZOMBIE_HUNTER_SNOWBALL_SPLAT.PAM";
        String pamPath2 = "768/INITIAL/EFFECTS/ZOMBIE_HUNTER_SNOWBALL_SPLAT/ZOMBIE_HUNTER_SNOWBALL_SPLAT.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPath1, pamPath2);


        actor.setSize(80, 80);
        actor.setOrigin(Align.center);

        Position pos = new Position(col, row);

        float x = pos.getX() - (actor.getWidth() / 2f);
        float y = pos.getY() - (actor.getHeight() / 2f) + 40;

        actor.setPosition(x, y);
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(0.63f),
            Actions.removeActor()
        ));
    }

    private void spawnHitSplash(GameEventPayload payload) {
        HitAnim hitAnim = HIT_ANIMS.get(payload.getProjectileType());
        if (hitAnim == null) return;

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            hitAnim.clip(), hitAnim.path());

        actor.setSize(HIT_SPLASH_SIZE, HIT_SPLASH_SIZE);
        actor.setOrigin(Align.center);

        float x = payload.getPixelX();
        float y = payload.getPixelY();
        actor.setPosition(x - actor.getWidth() / 2f, y - actor.getHeight() / 2f + 40f);

        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(hitAnim.duration()),
            Actions.removeActor()
        ));
    }


    public void sync(Arena arena) {
        if (arena == null) return;

        environmentRenderer.sync(arena);
        syncLawnMowers(arena.getLawnMowers());
        syncBrains(arena);
        syncRedLine(arena);
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
        projectileActorTypes.clear();
        sunActors.clear();
        plantChillOverlays.clear();
        plantFreezeOverlays.clear();
        plantOctopusOverlays.clear();
        plantSheepOverlays.clear();
        zombieActorTypes.clear();

        for (Image actor : brainActors.values()) actor.remove();
        brainActors.clear();
        if (redLineActor != null) redLineActor.remove();
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
                actor = UiFactory.imageFor(AssetLoader.getInstance().getTextures(),
                    "IMAGE_ZOMBIE_POWER_BRAIN_PROJECTILE_POWER_BRAIN_PROJECTILE_112X82");

                actor.setScale(1.2f);
                mowerLayer.addActor(actor);
                brainActors.put(brain, actor);
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
        } else {
            if (redLineActor != null) redLineActor.setVisible(false);
            return;
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
                removePlantIceOverlay(entry.getKey());

                PamAnimatedActor octopusActor = plantOctopusOverlays.remove(entry.getKey());
                if (octopusActor != null) octopusActor.remove();

                PamAnimatedActor sheepActor = plantSheepOverlays.remove(entry.getKey());
                if (sheepActor != null) sheepActor.remove();

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

        if (plant.isBoosted()) {
            if (anim.hasClip("plantfood_idle")) return "plantfood_idle";
            if (anim.hasClip("plantfood_stage1")) return "plantfood_stage1";
            if (anim.hasClip("plantfood_on")) return "plantfood_on";
            if (anim.hasClip("plantfood")) return "plantfood";
            if (anim.hasClip("plantfood_start")) return "plantfood_start";

        }

        TrapStrategy trap =
            plant.getStrategy(TrapStrategy.class);
        if (trap != null && !trap.isArmed()) {
            if (anim.hasClip("plant_idle")) return "plant_idle";
            if (anim.hasClip("plant")) return "plant";
        }

        DigestionStrategy digestion =
            plant.getStrategy(DigestionStrategy.class);
        if (digestion != null && digestion.isDigesting()) {
            if (anim.hasClip("special_idle")) return "special_idle";
            if (anim.hasClip("special")) return "special";
        }

        SunProductionStrategy sunProductionStrategy = plant.getStrategy(SunProductionStrategy.class);
        if (sunProductionStrategy != null && plant.getCurrentAction() != null && plant.getCurrentAction().equalsIgnoreCase("special")) {
            if (anim.hasClip("special_stage2")) return "special_stage2";
            if (anim.hasClip("special_idle")) return "special_idle";
            if (anim.hasClip("special")) return "special";
        }

        String action = plant.getCurrentAction();
        if (action != null && anim.hasClip(action))
            return action;


        if (plant.isAsleep() && anim.hasClip("sleep"))
            return "sleep";


        if (anim.hasClip("idle")) return "idle";
        if (anim.hasClip("loop")) return "loop";
        if (anim.hasClip("idle_stage1")) return "idle_stage1";
        if (anim.hasClip("idle1_1")) return "idle1_1";

        Iterator<String> anyClip = anim.getClipNames().iterator();
        return anyClip.hasNext() ? anyClip.next() : CLIP_IDLE;
    }

    private void syncZombies(List<Zombie> liveZombies) {
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
            updateZombieActor(zombie, actor);
        }

        Set<Zombie> stillAlive = new HashSet<>(liveZombies);
        Iterator<Map.Entry<Zombie, PamAnimatedActor>> it = zombieActors.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Zombie, PamAnimatedActor> entry = it.next();
            Zombie zombie = entry.getKey();
            PamAnimatedActor actor = entry.getValue();

            if (!stillAlive.contains(zombie)) {
                String deathClip = resolveZombieClip(zombie);
                actor.setClip(deathClip);

                float lingerTime = DESPAWN_LINGER_SECONDS;
                AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie);

                if (anim != null && anim.hasClip(deathClip)) {
                    lingerTime = anim.getDuration(deathClip);
                }

                despawn(actor, lingerTime - 0.5f);
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
        if (zombie.getCurrentSpeed() < 0) actor.setScale(-1);
        zombieLayer.addActor(actor);
        return actor;
    }

    private void updateZombieActor(Zombie zombie, PamAnimatedActor actor) {
        actor.setClip(resolveZombieClip(zombie));
        centerOnPoint(actor, zombie.getPosition().getX(), zombie.getPosition().getY() + actor.getHeight() / 2f + 30);

        if (!zombie.isDead()) {
            updateZombieArmorVisuals(zombie, actor);
        } else {
            actor.setVisibilityMap(null);
        }
    }

    private String resolveZombieClip(Zombie zombie) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getZombieAnimation(zombie.getType());

        if (zombie.isDead()) return pickClip(anim, CLIP_WALK, "die");

        if (zombie.getState() == ZombieState.TOSS) return pickClip(anim, CLIP_IDLE, "toss");

        if (zombie.getState() == ZombieState.INTRO) return pickClip(anim, "idle", "intro");
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
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getSunAnimation(sun.getType());

        String pamPath1 = anim != null ? anim.path : "768/FULL/EFFECTS/SUN/SUN.PAM";
        String pamPath2 = anim != null ? anim.path.replace("FULL", "INITIAL") : "768/INITIAL/EFFECTS/SUN/SUN.PAM";

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

            if (currentAbsorbTime < transitionDuration) {
                if (!actor.getClip().equals("transition_red")) {
                    actor.setClip("transition_red");
                }
            } else {
                if (!actor.getClip().equals("red")) {
                    actor.setClip("red");
                }
            }
        } else {
            if (!actor.getClip().equals("animation")) {
                actor.setClip("animation");
            }
        }

        if (!sun.isFalling() || sun.isBeingAbsorbed()) {
            float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
            float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;
            actor.setPosition(targetX, targetY);
        }
    }

    private void syncProjectiles(List<Projectile> liveProjectiles) {
        for (Projectile proj : liveProjectiles) {
            PamAnimatedActor actor = projectileActors.get(proj);
            ProjectileType lastRenderedType = projectileActorTypes.get(proj);

            if (actor == null || lastRenderedType != proj.getType()) {
                if (actor != null) {
                    actor.remove();
                }
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

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            anim.clip(), anim.path());

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

    private record ProjectileAnim(String path, String clip) {
    }

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

    private static Map<ProjectileType, HitAnim> buildHitAnimMap() {
        Map<ProjectileType, HitAnim> map = new EnumMap<>(ProjectileType.class);
        map.put(ProjectileType.PEA, new HitAnim(Ids.ProjectileHits.PEA, "animation", 0.8333f));
        map.put(ProjectileType.ICE_PEA, new HitAnim(Ids.ProjectileHits.ICE_PEA, "animation", 0.625f));
        map.put(ProjectileType.ROTOBAGA_SEED, new HitAnim(Ids.ProjectileHits.ROTOBAGA_SEED, "animation", 0.5f));
        map.put(ProjectileType.FIRE_PEA, new HitAnim(Ids.ProjectileHits.FIRE_PEA, "animation", 0.625f));
        map.put(ProjectileType.GOO_PEA, new HitAnim(Ids.ProjectileHits.GOO_PEA, "animation", 0.4667f));
        map.put(ProjectileType.MAGIC_BEAM, new HitAnim(Ids.ProjectileHits.MAGIC_BEAM, "animation", 2.2f));
        map.put(ProjectileType.LIGHTNING_CLOUD, new HitAnim(Ids.ProjectileHits.LIGHTNING_CLOUD, "animation", 0.5f));
        map.put(ProjectileType.CABBAGE, new HitAnim(Ids.ProjectileHits.CABBAGE, "animation", 0.6667f));
        map.put(ProjectileType.CORN, new HitAnim(Ids.ProjectileHits.CORN, "animation", 0.6667f));
        map.put(ProjectileType.BUTTER, new HitAnim(Ids.ProjectileHits.BUTTER, "animation", 0.6667f));
        map.put(ProjectileType.MELON, new HitAnim(Ids.ProjectileHits.MELON, "animation", 0.6667f));
        map.put(ProjectileType.WINTER_MELON, new HitAnim(Ids.ProjectileHits.WINTER_MELON, "animation", 0.8333f));
        map.put(ProjectileType.PEPPER, new HitAnim(Ids.ProjectileHits.PEPPER, "animation", 1.2667f));
        map.put(ProjectileType.GRAPE, new HitAnim(Ids.ProjectileHits.GRAPE, "animation", 0.9f));
        map.put(ProjectileType.FUME, new HitAnim(Ids.ProjectileHits.FUME, "animation", 0.4667f));
        map.put(ProjectileType.PLASMA_BALL, new HitAnim(Ids.ProjectileHits.PLASMA_BALL, "animation", 1.3f));
        map.put(ProjectileType.EXPLODE_NUT_BOWL, new HitAnim(Ids.ProjectileHits.EXPLODE_NUT_BOWL, "explosion", 1.6667f));
        // SPIKE, WALLNUT_BOWL, GIANT_NUT_BOWL intentionally have no splash asset (blunt/piercing hits, no PAM splat exists)
        return map;
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

        centerOnPoint(actor, mower.getPosition().getX() + offsetX, mower.getPosition().getY() + 100f);
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
}
