package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.scenes.scene2d.Group;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class BattlefieldRenderer implements GameEventListener {

    private final Group masterGroup = new Group();
    private final Group environmentLayer = new Group();
    private final Group highlightLayer = new Group();
    private final Group mowerLayer = new Group();
    private final Group plantLayer = new Group();
    private final Group zombieLayer = new Group();
    private final Group effectLayer = new Group();

    private final EnvironmentRenderer environmentRenderer;
    private final WorldItemRenderer worldItemRenderer;
    private final PlantRenderer plantRenderer;
    private final ZombieRenderer zombieRenderer;
    private final EffectRenderer effectRenderer;

    public BattlefieldRenderer() {
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
            } else if ("SANDSTORM_START".equals(payload.getMessage())) {
                effectRenderer.spawnSandstormEffect(payload.getZombie());
            } else if ("TARGET_LOCKED".equals(payload.getMessage())) {
                effectRenderer.spawnTargetMarker(payload.getCol(), payload.getRow());
            } else if ("MISSILE_LAUNCHED".equals(payload.getMessage())) {
                effectRenderer.spawnFallingMissile(payload.getCol(), payload.getRow());
            } else if ("MISSILE_EXPLOSION".equals(payload.getMessage())) {
                effectRenderer.spawnMissileExplosion(payload.getPixelX(), payload.getPixelY());
            }else if("ICE_MISSILE_LAUNCHED".equals(payload.getMessage())) {
                effectRenderer.spawnFallingMissile(payload.getCol(), payload.getRow());
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
