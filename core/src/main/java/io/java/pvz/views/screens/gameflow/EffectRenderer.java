package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;

import java.util.EnumMap;
import java.util.Map;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class EffectRenderer {

    private static final float HIT_SPLASH_SIZE = 70f;
    private final Group effectLayer;

    private record HitAnim(String path, String clip, float duration) {
    }

    private static final Map<ProjectileType, HitAnim> HIT_ANIMS = buildHitAnimMap();

    public EffectRenderer(Group effectLayer) {
        this.effectLayer = effectLayer;
    }

    public void spawnPianoBreakEffect(float x, float y) {
        String pamPath = "768/FULL/ZOMBIE/PIANO/PIANO.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "die", pamPath);
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        actor.setPosition(x - actor.getWidth() / 2f, y - actor.getHeight() / 2f + 30f);

        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(3.0f),
            Actions.removeActor()
        ));
    }

    public void spawnArcadeBreakEffect(float x, float y) {
        String pamPath = "768/FULL/EFFECTS/80S_ARCADE_CABINET/80S_ARCADE_CABINET.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "death", pamPath);
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        actor.setPosition(x - actor.getWidth() / 2f, y - actor.getHeight() / 2f + 30f);

        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(3.5f),
            Actions.removeActor()
        ));
    }

    public void spawnBarrelBreakEffect(float x, float y) {
        String pamPath = "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER_BARREL/ZOMBIE_PIRATE_BARREL_PUSHER_BARREL.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "die", pamPath);
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        actor.setPosition(x - actor.getWidth() / 2f, y - actor.getHeight() / 2f + 30f);

        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(2.0f),
            Actions.removeActor()
        ));
    }

    public void spawnCrystalSkullBeamEffect(Zombie zombie) {
        if (zombie == null) return;

        String pamPath = "768/FULL/EFFECTS/CRYSTALSKULL_BEAM/CRYSTALSKULL_BEAM.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "laser_beam", pamPath);

        actor.setSize(TILE_WIDTH * 4, TILE_HEIGHT);
        actor.setOrigin(Align.right);

        float x = zombie.getPosition().getX() - actor.getWidth();
        float y = zombie.getPosition().getY() + 40f;

        actor.setPosition(x, y);
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(0.6667f),
            Actions.removeActor()
        ));
    }

    public void spawnDeflectedProjectileVisual(Zombie zombie, Plant plant, ProjectileType type) {
        if (zombie == null || plant == null || type == null) return;

        WorldItemRenderer.ProjectileAnim anim = WorldItemRenderer.getProjectileAnim(type);

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

    public void spawnIceBlockDamageEffect(int col, int row) {
        String pamPath1 = "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PARTICLES/FROSTBITE_ICE_BLOCK_PARTICLES.PAM";
        String pamPath2 = "768/INITIAL/EFFECTS/FROSTBITE_ICE_BLOCK_PARTICLES/FROSTBITE_ICE_BLOCK_PARTICLES.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPath1, pamPath2);

        actor.setSize(80, 80);
        actor.setOrigin(Align.center);

        Position pos = new Position(col, row);
        float x = pos.getX() - (actor.getWidth() / 2f);
        float y = pos.getY() - (actor.getHeight() / 2f) + 15f;

        actor.setPosition(x, y);
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.delay(1.0f),
            Actions.removeActor()
        ));
    }

    public void spawnBoneHitEffect(int col, int row) {
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

    public void spawnHunterIceHitEffect(int col, int row) {
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

    public void spawnHitSplash(GameEventPayload payload) {
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

    public void spawnWindEffect(int row) {
        float y = GRID_START_Y + (row + 1) * TILE_HEIGHT + TILE_HEIGHT / 2f;

        String pamPath = "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), "animation", pamPath);

        actor.setSize(TILE_WIDTH * 9, TILE_HEIGHT);
        actor.setOrigin(Align.center);

        float startX = GRID_START_X + (9 * TILE_WIDTH) + 200f;
        float targetX = GRID_START_X - (9 * TILE_WIDTH) - 200f;

        actor.setPosition(startX, y - (actor.getHeight() / 2f));
        effectLayer.addActor(actor);

        actor.addAction(Actions.sequence(
            Actions.moveTo(targetX, actor.getY(), 2f, Interpolation.linear),
            Actions.removeActor()
        ));
    }

    public void spawnSandstormEffect(Zombie zombie) {
        if (zombie == null) return;

        PamAnimatedActor tornado = new PamAnimatedActor(
            AssetLoader.getInstance().getPlayer(),
            "loop",
            Ids.ArenaEffects.SANDSTORM
        );

        tornado.setSize(TILE_WIDTH * 1.2f, TILE_HEIGHT * 2.3f);
        tornado.setOrigin(Align.center);

        effectLayer.addActor(tornado);

        tornado.addAction(Actions.forever(Actions.run(() -> {
            if (zombie.isDead() || zombie.getSpawnEffect() != Zombie.SpawnEffect.SANDSTORM) {
                tornado.clearActions();
                tornado.remove();
                return;
            }

            float x = zombie.getX() - (tornado.getWidth() / 2f);
            float y = GRID_START_Y + ((zombie.getRow() + 1) * TILE_HEIGHT) + 20f;

            tornado.setPosition(x, y);
        })));
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
        map.put(ProjectileType.EXPLODE_NUT_BOWL, new HitAnim(Ids.ProjectileHits.EXPLODE_NUT_BOWL,
            "explosion", 1.6667f));
        return map;
    }
}
