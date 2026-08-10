package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.projectiles.NormalEffect;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;

public class ExplosiveStrategy implements IPlantStrategy {

    private static final int EXPLOSION_DELAY_TICKS = TimeManager.TICKS_PER_SECOND; // 1 sec delay for animation
    private int startTick = -1;
    private int extraBounces = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) {
            startTick = currentTick;
        }

        if (currentTick - startTick >= EXPLOSION_DELAY_TICKS) {
            String name = context.getName();
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();

            int damage = context.getDamage() > 0 ? context.getDamage() : 1800;

            notify("💥 " + name + " DETONATED!");

            switch (name) {
                case "Cherry Bomb", "Grapeshot":
                    handleCherryBombAndGrapeshot(name, context, plantCol, plantRow, damage);
                    break;
                case "Jalapeno":
                    handleJalapeno(context, plantRow, damage);
                    break;
                case "Doom-shroom":
                    handleDoomShroom(context, plantCol, plantRow, damage);
                    break;
                default:

                    break;
            }

            context.takeDamage(context.getCurrentHp());
        }
    }

    private void handleCherryBombAndGrapeshot(String name, Plant context, int plantCol, int plantRow, int damage) {
        applyAreaDamage(plantCol, plantRow, 1.5f, damage, context);

        if ("Grapeshot".equals(name)) {
            spawnGrapeshotProjectiles(context, plantCol, plantRow);
            notify("🍇 Grapeshot scattered 8 bouncing grapes in all directions!");
        }
    }

    private void spawnGrapeshotProjectiles(Plant context, int plantCol, int plantRow) {
        float[][] directions = {
            {1.0f, 0.0f}, {-1.0f, 0.0f}, {0.0f, 1.0f}, {0.0f, -1.0f},
            {0.707f, 0.707f}, {-0.707f, 0.707f}, {0.707f, -0.707f}, {-0.707f, -0.707f}
        };

        int finalBounceLimit = 3 + extraBounces;

        for (float[] dir : directions) {
            Projectile grape = new Projectile(
                context,
                ProjectileType.GRAPE,
                new NormalEffect(),
                60,
                new Position(plantCol, plantRow),
                dir[0] * ProjectileTuning.GRAPE_SPEED_TILES_PER_SEC,
                dir[1] * ProjectileTuning.GRAPE_SPEED_TILES_PER_SEC,
                false,
                false
            );

            grape.setLifespanTicks(5 * TimeManager.TICKS_PER_SECOND);
            grape.setBouncesLeft(finalBounceLimit);

            Projectile.spawnCustom(grape);
        }
    }

    private void handleJalapeno(Plant context, int plantRow, int damage) {
        Arena arena = GameSession.getInstance().getArena();

        for (Zombie z : arena.zombieInRow(plantRow)) {
            if (!z.isDead()) {
                z.removeChillEffect();
                z.removeFreezeEffect();
                z.takeDamage(damage);

                if (z.isDead()) {
                    context.onZombieDeath(z);
                }
            }
        }

        for (int col = 0; col < arena.getCols(); col++) {
            Tile tile = arena.getTile(plantRow, col);
            if (tile instanceof IceHolder iceHolder && iceHolder.hasIceBlock()) {
                iceHolder.takeIceDamage(9999);
            }
        }

        notify("🔥 Jalapeno burned the entire lane!");
    }

    private void handleDoomShroom(Plant context, int plantCol, int plantRow, int damage) {
        applyAreaDamage(plantCol, plantRow, 3.5f, damage, context);

        Tile doomedTile = GameSession.getInstance().getArena().getTile(plantRow, plantCol);
        if (doomedTile != null) {
            doomedTile.setCrater(true);
        }

        notify("🕳️ Doom-shroom left a massive crater behind!");
    }

    private void applyAreaDamage(int col, int row, float radius, int damage, Plant plant) {
        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(col, row, radius);
        for (Zombie z : targets) {
            if (!z.isDead()) {
                z.takeDamage(damage);
                if (z.isDead()) {
                    plant.onZombieDeath(z);
                }
            }
        }
    }

    public void increaseBounceLimit(int amount) {
        this.extraBounces += amount;
    }
}
