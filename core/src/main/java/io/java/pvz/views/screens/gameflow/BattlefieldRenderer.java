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
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.HashMap;
import java.util.Map;

public class BattlefieldRenderer implements GameEventListener {

    private final Group masterGroup = new Group();
    private final Group environmentLayer = new Group();
    private final Group highlightLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group plantLayer;
    private final Group zombieLayer;
    private final Group effectLayer = new Group();

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
        ShaderProgram.pedantic = false;
        entityShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/effects.frag")
        );

        if (!entityShader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed:\n" + entityShader.getLog());
        }

        plantLayer = new Group() {
            @Override
            public void drawChildren(Batch batch, float parentAlpha) {
                batch.setShader(entityShader);
                for (Actor child : getChildren()) {
                    if (!child.isVisible()) continue;
                    Plant plant = (Plant) child.getUserObject();
                    if (plant != null) {
                        float flash = plantFlashTimers.getOrDefault(plant, 0f) > 0 ? 1f : 0f;
                        entityShader.setUniformf("u_tintColor", 1f, 1f, 1f, 0f);
                        entityShader.setUniformf("u_damageFlash", flash);
                    }
                    child.draw(batch, parentAlpha);
                }
                batch.setShader(null);
            }
        };

        zombieLayer = new Group() {
            @Override
            public void drawChildren(Batch batch, float parentAlpha) {
                batch.setShader(entityShader);
                for (Actor child : getChildren()) {
                    if (!child.isVisible()) continue;
                    Zombie zombie = (Zombie) child.getUserObject();
                    if (zombie != null) {
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

                            r = 1.0f; g = 0.0f; b = 0.0f;
                            intensity = (float) (Math.abs(Math.sin(System.currentTimeMillis() / 150.0)) * 0.4 + 0.2);
                        } else if (zombie.isHypnotized()) {
                            r = 1.0f; g = 0.4f; b = 1.0f; intensity = 0.5f;
                        } else if (isFrozen) {
                            r = 0.2f; g = 0.5f; b = 1.0f; intensity = 0.5f;
                        } else if (isChilled) {
                            r = 0.5f; g = 0.8f; b = 1.0f; intensity = 0.3f;
                        } else if (isPoisoned) {
                            r = 0.6f; g = 0.1f; b = 0.8f; intensity = 0.5f;
                        }

                        float flash = zombieFlashTimers.getOrDefault(zombie, 0f) > 0 ? 1f : 0f;

                        if (zombie.isDead()) {
                            flash = 0f;
                            r = 1f; g = 1f; b = 1f; intensity = 0f;
                        }

                        entityShader.setUniformf("u_tintColor", r, g, b, intensity);
                        entityShader.setUniformf("u_damageFlash", flash);
                    }
                    child.draw(batch, parentAlpha);
                }
                batch.setShader(null);
            }
        };
        masterGroup.addActor(environmentLayer);
        masterGroup.addActor(highlightLayer);
        masterGroup.addActor(mowerLayer);
        masterGroup.addActor(plantLayer);
        masterGroup.addActor(zombieLayer);
        masterGroup.addActor(effectLayer);

        environmentRenderer = new EnvironmentRenderer(environmentLayer);
        effectRenderer = new EffectRenderer(effectLayer);
        worldItemRenderer = new WorldItemRenderer(effectLayer, mowerLayer, zombieLayer, highlightLayer);
        plantRenderer = new PlantRenderer(plantLayer);
        zombieRenderer = new ZombieRenderer(zombieLayer);

        GameEventMessenger.getInstance().addListener(GameEvent.PROJECTILE_HIT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.SPAWN_EFFECT, this);
        GameEventMessenger.getInstance().addListener(GameEvent.NOTIFY, this);
    }

    public Group getGroup() {
        return masterGroup;
    }

    public Group getHighlightLayer() {
        return highlightLayer;
    }

    public void sync(Arena arena) {
        if (arena == null) return;
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
    }

    public void clear() {
        environmentRenderer.clear();
        worldItemRenderer.clear();
        plantRenderer.clear();
        zombieRenderer.clear();
        effectLayer.clearChildren();
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.PROJECTILE_HIT) {
            effectRenderer.spawnHitSplash(payload);

            if (payload.getZombie() != null) {
                zombieFlashTimers.put(payload.getZombie(), 0.15f);
            }

            if (payload.getPlant() != null) {
                plantFlashTimers.put(payload.getPlant(), 0.15f);
            }
        } else if (event == GameEvent.SPAWN_EFFECT) {
            System.out.println("Effect Received! Type: " + payload.getMessage());


            if ("BONE_HIT".equals(payload.getMessage())) {
                effectRenderer.spawnBoneHitEffect(payload.getCol(), payload.getRow());
            } else if ("HUNTER_SNOWBALL_HIT".equals(payload.getMessage())) {
                effectRenderer.spawnHunterIceHitEffect(payload.getCol(), payload.getRow());
            } else if ("UPDATE_ICE_OVERLAY".equals(payload.getMessage())) {
                plantRenderer.updatePlantIceOverlay(payload.getPlant(), payload.getAmount());
            } else if ("REMOVE_ICE_OVERLAY".equals(payload.getMessage())) {
                plantRenderer.removePlantIceOverlay(payload.getPlant());
            } else if ("ICE_BLOCK_DAMAGE".equals(payload.getMessage())) {
                effectRenderer.spawnIceBlockDamageEffect(payload.getCol(), payload.getRow());
            } else if ("UPDATE_ICE_CRACKS".equals(payload.getMessage())) {
                plantRenderer.updateIceCracks(payload.getPlant(), payload.getAmount());
            } else if ("OCTOPUS_LAND".equals(payload.getMessage())) {
                plantRenderer.spawnOctopusOnPlant(payload.getPlant());
            } else if ("OCTOPUS_DIE".equals(payload.getMessage())) {
                plantRenderer.killOctopusOnPlant(payload.getPlant());
            } else if ("CRYSTAL_SKULL_BEAM".equals(payload.getMessage())) {
                effectRenderer.spawnCrystalSkullBeamEffect(payload.getZombie());
            } else if ("BARREL_BREAK".equals(payload.getMessage())) {
                effectRenderer.spawnBarrelBreakEffect(payload.getPixelX(), payload.getPixelY());
            } else if ("ARCADE_MACHINE_BREAK".equals(payload.getMessage())) {
                effectRenderer.spawnArcadeBreakEffect(payload.getPixelX(), payload.getPixelY());
            } else if ("PIANO_BREAK".equals(payload.getMessage())) {
                effectRenderer.spawnPianoBreakEffect(payload.getPixelX(), payload.getPixelY());
            } else if ("FREEZING_WIND".equals(payload.getMessage())) {
                effectRenderer.spawnWindEffect(payload.getRow());
            }else if ("BOSS_WIND".equals(payload.getMessage())){
                effectRenderer.spawnWindEffect(payload.getRow());
                effectRenderer.spawnWindEffect(payload.getCol());
            }else if ("SANDSTORM_START".equals(payload.getMessage())) {
                effectRenderer.spawnSandstormEffect(payload.getZombie());
            } else if ("TARGET_LOCKED".equals(payload.getMessage())) {
                effectRenderer.spawnTargetMarker(payload.getCol(), payload.getRow());
            } else if ("MISSILE_LAUNCHED".equals(payload.getMessage())) {
                effectRenderer.spawnFallingMissile(payload.getCol(), payload.getRow());
            } else if ("MISSILE_EXPLOSION".equals(payload.getMessage())) {
                effectRenderer.spawnMissileExplosion(payload.getPixelX(), payload.getPixelY());
            }else if("ICE_MISSILE_LAUNCHED".equals(payload.getMessage())) {
                effectRenderer.spawnFallingMissile(payload.getCol(), payload.getRow());
            } else if ("FIREBALL_LAUNCHED".equals(payload.getMessage())) {
                effectRenderer.spawnFallingFireball(payload.getCol(), payload.getRow());
            } else if ("FIREBALL_EXPLOSION".equals(payload.getMessage())) {
                effectRenderer.spawnFireballExplosion(payload.getCol(), payload.getRow());
            }
        } else if (event == GameEvent.NOTIFY && payload.getMessage() != null) {
            String msg = String.valueOf(payload.getMessage());

            if ("DEFLECT_PROJECTILE".equals(msg)) {
                effectRenderer.spawnDeflectedProjectileVisual(payload.getZombie(),
                    payload.getPlant(), payload.getProjectileType());
            } else if ("SHEEP_APPLY".equals(msg)) {
                plantRenderer.spawnSheepOnPlant(payload.getPlant());
            } else if ("SHEEP_REMOVE".equals(msg)) {
                plantRenderer.removeSheepFromPlant(payload.getPlant());
            } else if ("IMP_THROWN".equals(msg)) {
                zombieRenderer.animateImpFlight(payload.getZombie(), payload.getPixelX(), payload.getPixelY());
            }
        }
    }
}
