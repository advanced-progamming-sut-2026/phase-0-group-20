package models.entities.plants.strategy.category_strategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.NormalEffect;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.fields.obstacle.IceHolder;
import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

/**
 * Explosive Strategy:
 * Used for instant-kill plants like Cherry Bomb.
 * Triggers a massive explosion in a specific area shortly after being planted,
 * then instantly kills the plant itself.
 */

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

        GameSession session = GameSession.getInstance();
        TimeManager timeManager = session.getTimeManager();
        Arena arena = session.getArena();

        int finalBounceLimit = 3 + extraBounces;

        for (float[] dir : directions) {
            Projectile grape = new Projectile(
                    context,
                    ProjectileType.GRAPE,
                    new NormalEffect(),
                    60,
                    new Position(plantCol, plantRow),
                    dir[0] * 2.5f,
                    dir[1] * 2.5f,
                    false,
                    false
            );

            grape.setLifespanTicks(5 * TimeManager.TICKS_PER_SECOND);
            grape.setBouncesLeft(finalBounceLimit);

            timeManager.registerNewTicker(grape);
            arena.addProjectile(grape);
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
