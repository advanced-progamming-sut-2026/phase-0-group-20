package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.ChillEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.PoisonEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class BattlefieldRenderer implements GameEventListener {

    private final Group masterGroup = new Group();
    private final Group environmentLayer = new Group();
    private final Group highlightLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group boardLayer;
    private final Group effectLayer = new Group();
    private final Group topLayer = new Group();

    private final EnvironmentRenderer environmentRenderer;
    private final WorldItemRenderer worldItemRenderer;
    private final PlantRenderer plantRenderer;
    private final ZombieRenderer zombieRenderer;
    private final EffectRenderer effectRenderer;


    private ShaderProgram entityShader;

    private final Map<Zombie, Integer> zombieLastHp = new HashMap<>();
    private final Map<Zombie, Float> zombieFlashTimers = new HashMap<>();
    private final Map<Plant, Integer> plantLastHp = new HashMap<>();
    private final Map<Plant, Float> plantFlashTimers = new HashMap<>();

    public BattlefieldRenderer() {
        initShader();

        boardLayer = getBoardLayer();

        masterGroup.addActor(environmentLayer);
        masterGroup.addActor(highlightLayer);
        masterGroup.addActor(mowerLayer);
        masterGroup.addActor(effectLayer);
        masterGroup.addActor(boardLayer);
        masterGroup.addActor(topLayer);

        environmentRenderer = new EnvironmentRenderer(environmentLayer);
        worldItemRenderer = new WorldItemRenderer(effectLayer, boardLayer, topLayer, mowerLayer, highlightLayer);

        plantRenderer = new PlantRenderer(boardLayer);
        zombieRenderer = new ZombieRenderer(boardLayer);

        effectRenderer = new EffectRenderer(topLayer);

        GameEventMessenger.getInstance().addListener(GameEvent.PROJECTILE_HIT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.SPAWN_EFFECT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.NOTIFY, this);
    }

    private @NonNull Group getBoardLayer() {
        return new Group() {
            @Override
            public void drawChildren(Batch batch, float parentAlpha) {
                batch.setShader(entityShader);
                for (Actor child : getChildren()) {
                    if (!child.isVisible()) continue;

                    boolean isSpawningZombie = false;
                    float groundClipY = 0f;

                    Object userObj = child.getUserObject();
                    if (userObj instanceof Plant plant) {
                        applyPlantShaderUniforms(plant);
                    } else if (userObj instanceof Zombie zombie) {
                        applyZombieShaderUniforms(zombie);

                        if (zombie.isSpawning() && zombie.getSpawnEffect() != Zombie.SpawnEffect.SANDSTORM) {
                            isSpawningZombie = true;
                            groundClipY = zombie.getPosition().getY() - 50f;
                        }

                    } else {
                        entityShader.setUniformf("u_tintColor", 1f, 1f, 1f, 0f);
                        entityShader.setUniformf("u_damageFlash", 0f);
                    }

                    if (isSpawningZombie) {
                        batch.flush();
                        if (clipBegin(child.getX() - 150f, groundClipY, child.getWidth() + 300f, child.getHeight() + 200f)) {
                            child.draw(batch, parentAlpha);
                            batch.flush();
                            clipEnd();
                        }
                    } else
                        child.draw(batch, parentAlpha);

                }
                batch.setShader(null);
            }
        };
    }

    private void initShader() {
        ShaderProgram.pedantic = false;
        entityShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/effects.frag")
        );

        if (!entityShader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed:\n" + entityShader.getLog());
        }
    }

    private void applyPlantShaderUniforms(Plant plant) {
        float flash = plantFlashTimers.getOrDefault(plant, 0f) > 0 ? 1f : 0f;
        entityShader.setUniformf("u_tintColor", 1f, 1f, 1f, 0f);
        entityShader.setUniformf("u_damageFlash", flash);
    }

    private void applyZombieShaderUniforms(Zombie zombie) {
        float r = 1f, g = 1f, b = 1f, intensity = 0f;
        boolean isPoisoned = false, isFrozen = false, isChilled = false;

        if (zombie.getActiveEffects() != null) {
            for (ZombieEffect effect : zombie.getActiveEffects()) {
                if (effect instanceof PoisonEffect) isPoisoned = true;
                if (effect instanceof FreezeEffect) isFrozen = true;
                if (effect instanceof ChillEffect) isChilled = true;
            }
        }

        int col = zombie.getCol();

        if (col <= 1) {
            r = 1.0f;
            g = 0.0f;
            b = 0.0f;
            intensity = (float) (Math.abs(Math.sin(System.currentTimeMillis() / 150.0)) * 0.4 + 0.2);
        } else if (zombie.isHypnotized()) {
            r = 1.0f;
            g = 0.4f;
            b = 1.0f;
            intensity = 0.5f;
        } else if (isFrozen) {
            r = 0.2f;
            g = 0.5f;
            b = 1.0f;
            intensity = 0.5f;
        } else if (isChilled) {
            r = 0.5f;
            g = 0.8f;
            b = 1.0f;
            intensity = 0.3f;
        } else if (isPoisoned || zombie.isShiny()) {
            r = 0.6f;
            g = 0.1f;
            b = 0.8f;
            intensity = 0.5f;
        }

        float flash = zombieFlashTimers.getOrDefault(zombie, 0f) > 0 ? 1f : 0f;

        if (zombie.isDead()) {
            flash = 0f;
            r = 1f;
            g = 1f;
            b = 1f;
            intensity = 0f;
        }

        entityShader.setUniformf("u_tintColor", r, g, b, intensity);
        entityShader.setUniformf("u_damageFlash", flash);
    }

    public Group getGroup() {
        return masterGroup;
    }

    public Group getHighlightLayer() {
        return highlightLayer;
    }

    public void sync(Arena arena) {
        if (arena == null) {
            return;
        }
        float delta = Gdx.graphics.getDeltaTime();

        for (Zombie z : arena.getActiveZombies()) {
            int currentHp = z.getHealth();
            Integer lastHp = zombieLastHp.getOrDefault(z, currentHp);
            if (currentHp < lastHp) {
                zombieFlashTimers.put(z, 0.15f);
            }
            zombieLastHp.put(z, currentHp);

            float timer = zombieFlashTimers.getOrDefault(z, 0f);
            if (timer > 0) zombieFlashTimers.put(z, timer - delta);
        }

        for (Plant p : arena.getActivePlants()) {
            int currentHp = p.getCurrentHp();
            Integer lastHp = plantLastHp.getOrDefault(p, currentHp);
            if (currentHp < lastHp) {
                plantFlashTimers.put(p, 0.15f);
            }
            plantLastHp.put(p, currentHp);

            float timer = plantFlashTimers.getOrDefault(p, 0f);
            if (timer > 0) plantFlashTimers.put(p, timer - delta);
        }

        environmentRenderer.sync(arena);
        worldItemRenderer.sync(arena);
        plantRenderer.syncPlants(arena.getActivePlants());
        zombieRenderer.syncZombies(arena.getActiveZombies());
        boardLayer.getChildren().sort((a, b) -> {
            float yA = a.getY() - (a.getUserObject() instanceof Zombie ? 3f : 0f);
            float yB = b.getY() - (b.getUserObject() instanceof Zombie ? 3f : 0f);
            return Float.compare(yB, yA);
        });
    }

    public void clear() {
        environmentRenderer.clear();
        worldItemRenderer.clear();
        plantRenderer.clear();
        zombieRenderer.clear();
        effectLayer.clearChildren();

        zombieLastHp.clear();
        zombieFlashTimers.clear();
        plantLastHp.clear();
        plantFlashTimers.clear();
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.PROJECTILE_HIT) {
            handleProjectileHitEvent(payload);
        } else if (event == GameEvent.SPAWN_EFFECT) {
            handleSpawnEffectEvent(payload);
        } else if (event == GameEvent.EFFECTS && payload.getMessage() != null) {
            handleNotifyEvent(payload);
        }
    }

    private void handleProjectileHitEvent(GameEventPayload payload) {
        effectRenderer.spawnHitSplash(payload);

        if (payload.getZombie() != null) {
            zombieFlashTimers.put(payload.getZombie(), 0.15f);
        }

        if (payload.getPlant() != null) {
            plantFlashTimers.put(payload.getPlant(), 0.15f);
        }
    }

    private void handleSpawnEffectEvent(GameEventPayload payload) {
        System.out.println("Effect Received! Type: " + payload.getMessage());

        String type = payload.getMessage();
        if (type == null) return;

        switch (type) {
            case "BONE_HIT" -> effectRenderer.spawnBoneHitEffect(payload.getCol(), payload.getRow());
            case "HUNTER_SNOWBALL_HIT" -> effectRenderer.spawnHunterIceHitEffect(payload.getCol(), payload.getRow());
            case "UPDATE_ICE_OVERLAY" -> plantRenderer.updatePlantIceOverlay(payload.getPlant(), payload.getAmount());
            case "REMOVE_ICE_OVERLAY" -> plantRenderer.removePlantIceOverlay(payload.getPlant());
            case "ICE_BLOCK_DAMAGE" -> effectRenderer.spawnIceBlockDamageEffect(payload.getCol(), payload.getRow());
            case "UPDATE_ICE_CRACKS" -> plantRenderer.updateIceCracks(payload.getPlant(), payload.getAmount());
            case "OCTOPUS_LAND" -> plantRenderer.spawnOctopusOnPlant(payload.getPlant());
            case "OCTOPUS_DIE" -> plantRenderer.killOctopusOnPlant(payload.getPlant());
            case "CRYSTAL_SKULL_BEAM" -> effectRenderer.spawnCrystalSkullBeamEffect(payload.getZombie());
            case "BARREL_BREAK" -> effectRenderer.spawnBarrelBreakEffect(payload.getPixelX(), payload.getPixelY());
            case "ARCADE_MACHINE_BREAK" ->
                effectRenderer.spawnArcadeBreakEffect(payload.getPixelX(), payload.getPixelY());
            case "PIANO_BREAK" -> effectRenderer.spawnPianoBreakEffect(payload.getPixelX(), payload.getPixelY());
            case "FREEZING_WIND" -> effectRenderer.spawnWindEffect(payload.getRow());
            case "BOSS_WIND" -> {
                effectRenderer.spawnWindEffect(payload.getRow());
                effectRenderer.spawnWindEffect(payload.getCol());
            }
            case "SANDSTORM_START" -> effectRenderer.spawnSandstormEffect(payload.getZombie());
            case "TARGET_LOCKED" -> effectRenderer.spawnTargetMarker(payload.getCol(), payload.getRow());
            case "MISSILE_LAUNCHED", "ICE_MISSILE_LAUNCHED" ->
                effectRenderer.spawnFallingMissile(payload.getCol(), payload.getRow());
            case "MISSILE_EXPLOSION" -> effectRenderer.spawnMissileExplosion(payload.getPixelX(), payload.getPixelY());
            case "FIREBALL_LAUNCHED" -> effectRenderer.spawnFallingFireball(payload.getCol(), payload.getRow());
            case "FIREBALL_EXPLOSION" -> effectRenderer.spawnFireballExplosion(payload.getCol(), payload.getRow());
            case "POTATOMINE_EXPLODE" -> effectRenderer.spawnPotatoMineExplosion(payload.getCol(), payload.getRow());
            case "PRIMAL_POTATOMINE_EXPLODE" ->
                effectRenderer.spawnPrimalPotatoMineExplosion(payload.getCol(), payload.getRow());
            case "CHERRYBOMB_EXPLODE" -> effectRenderer.spawnCherryBombExplosion(payload.getCol(), payload.getRow());
            case "JALAPENO_EXPLODE" -> effectRenderer.spawnJalapenoFire(payload.getRow());
            case "FUME_PLANTFOOD" -> effectRenderer.spawnFumePlantFoodEffect(payload.getCol(), payload.getRow());
        }
    }

    private void handleNotifyEvent(GameEventPayload payload) {
        String msg = String.valueOf(payload.getMessage());

        switch (msg) {
            case "DEFLECT_PROJECTILE" -> effectRenderer.spawnDeflectedProjectileVisual(
                payload.getZombie(), payload.getPlant(), payload.getProjectileType()
            );
            case "SHEEP_APPLY" -> plantRenderer.spawnSheepOnPlant(payload.getPlant());
            case "SHEEP_REMOVE" -> plantRenderer.removeSheepFromPlant(payload.getPlant());
            case "IMP_THROWN" -> zombieRenderer.animateImpFlight(
                payload.getZombie(), payload.getPixelX(), payload.getPixelY()
            );
        }
    }
}
