package io.java.pvz.models.game;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.obstacle.GraveHolder;
import io.java.pvz.models.entities.obstacle.PushableObstacle;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.TorchwoodStrategy;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.TrapStrategy;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.fields.LawnMower;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;

public class CollisionManager {
    private final GameSession session;
    private final Arena arena;
    private static final int LAWN_MOWER_THRESHOLD = 30;
    public CollisionManager(GameSession session) {
        this.session = session;
        this.arena = session.getArena();
    }

    public void checkAllCollisions() {
        List<Zombie> activeZombies = arena.getActiveZombies();

        handleLawnMowers(activeZombies);

        // for projectiles
        handleProjectiles();

        // for plants & zombies
        for (Zombie z : activeZombies) {
            if (z.isHypnotized()) {
                checkZombiesAndZombiesCollision(z);
            } else {
                checkZombiesAndPlantCollision(z);
            }
        }

        for (Sun sun : arena.getActiveSuns()) {
            checkSunCollision(sun);
        }
    }

    private void handleLawnMowers(List<Zombie> activeZombies) {
        LawnMower[] mowers = arena.getLawnMowers();
        if (mowers == null) return;

        for (LawnMower mower : mowers) {
            if (mower == null || mower.isDestroyed()) continue;

            for (Zombie z : activeZombies) {
                if (z.isDead() || z.getRow() != mower.getRow()) continue;

                if (!mower.isActivate() && z.getX() <= mower.getPosition().getX() + LAWN_MOWER_THRESHOLD) {
                    mower.trigger();
                    killZombieByMower(z, mower);
                }
                else if (mower.isActivate() && z.getX() <= mower.getPosition().getX() + LAWN_MOWER_THRESHOLD) {
                    killZombieByMower(z, mower);
                }
            }
        }
    }

    private void killZombieByMower(Zombie z, LawnMower mower) {
        z.takeDamage(10000, null);
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED_LAWN_MOWER)
            .zombie(z)
            .coordinate(mower.getRow(), z.getCol())
            .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, payload);
    }

    private void handleProjectiles() {
        for (Projectile proj : arena.getActiveProjectiles()) {
            if (proj.isDestroyed() || !proj.isSpawned()) continue;

            Tile currentTile = arena.getTile(proj.getPosition().getRow(), proj.getPosition().getCol());
            if (currentTile == null) continue;
            if (!proj.isFiredByZombie() && !proj.isGetTorchWood()) {
                for (Plant p : currentTile.getPlants()) {
                    for (IPlantStrategy strategy : p.getStrategies()) {
                        if (strategy instanceof TorchwoodStrategy torchwoodStrategy) {
                            torchwoodStrategy.igniteProjectile(proj);
                            proj.setGetTorchWood(true);
                        }
                    }
                }
            }
            Plant frozenPlantInTile = null;
            for (Plant p : currentTile.getPlants()) {
                if (p.isFrozen()) {
                    frozenPlantInTile = p;
                    break;
                }
            }
            if (frozenPlantInTile != null && !ProjectileType.isLobbed(proj.getType())) {

                frozenPlantInTile.damageIceBlock(proj.getDamage());

                proj.setDestroyed(true);
                continue;
            }
            Plant octopusPlantInTile = null;
            for (Plant p : currentTile.getPlants()) {
                if (p.hasOctopus()) {
                    octopusPlantInTile = p;
                    break;
                }
            }
            if (octopusPlantInTile != null && !ProjectileType.isLobbed(proj.getType())) {
                octopusPlantInTile.damageOctopus(proj.getDamage());
                proj.setDestroyed(true);
                continue;
            }
            if (proj.isFiredByZombie())
                checkProjectileForPlantCollision(proj);
            else {
                boolean hitObstacle = checkProjectileForObstaclesCollision(proj);
                if (!hitObstacle && !proj.isDestroyed())
                    checkProjectileForZombieCollision(proj);
            }
        }
    }

    private void checkProjectileForPlantCollision(Projectile projectile) {
        int row = projectile.getPosition().getRow();
        int col = projectile.getPosition().getCol();
        if (col < 0 || col >= arena.getCols()) return;

        Tile tile = arena.getTile(row, col);
        if (tile == null || tile.getPlants().isEmpty()) return;

        List<Plant> plantsHere = tile.getPlants();
        Plant target = plantsHere.getLast();

        projectile.onHit(target);
    }

    private void checkProjectileForZombieCollision(Projectile projectile) {
        boolean hitObstacle = false;
        for (PushableObstacle obstacle : arena.getActiveObstacles()) {
            if (!obstacle.isDestroyed() && obstacle.getRow() == projectile.getPosition().getRow()) {

                if (Math.abs(projectile.getPosition().getX() - obstacle.getX()) < 20) {

                    obstacle.takeDamage(projectile.getDamage());
                    projectile.setDestroyed(true);
                    hitObstacle = true;
                    break;
                }
            }
        }

        if (hitObstacle) return;

        float projectileHitRadius = 0.25f;
        float zombieHitRadius = 0.25f;
        float physProjectileRadius = projectileHitRadius * PhysicalConstants.TILE_WIDTH;
        float physZombieRadius = zombieHitRadius * PhysicalConstants.TILE_WIDTH;

        int bottomRow = (int) Math.floor((projectile.getY() - physProjectileRadius - PhysicalConstants.GRID_START_Y)
            / PhysicalConstants.TILE_HEIGHT);
        int topRow = (int) Math.floor((projectile.getY() + physProjectileRadius - PhysicalConstants.GRID_START_Y)
            / PhysicalConstants.TILE_HEIGHT);

        bottomRow = Math.max(0, bottomRow);
        topRow = Math.min(arena.getRows() - 1, topRow);

        List<Zombie> nearbyZombies = new ArrayList<>();
        for (int row = bottomRow; row <= topRow; row++)
            nearbyZombies.addAll(arena.zombieInRow(row));

        float combinedRadius = physProjectileRadius + physZombieRadius;

        for (Zombie z : nearbyZombies) {
            if (z.isDead()) continue;

            if (projectile.getTarget() != null && projectile.getTarget() != z) {
                continue;
            }

            double dx = projectile.getX() - z.getX();
            double dy = projectile.getY() - z.getY();
            double distanceSquared = (dx * dx) + (dy * dy);

            if (distanceSquared <= (combinedRadius * combinedRadius)) {
                projectile.onHit(z);
                if (!projectile.isPiercing() || projectile.isDestroyed()) break;
            }
        }
    }

    private boolean checkProjectileForObstaclesCollision(Projectile proj) {
        if (proj.canPassObstacles()) return false;

        int projectileRow = proj.getPosition().getRow();
        int projectileCol = (int) ((proj.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH);
        Tile currentTile = arena.getTile(projectileRow, projectileCol);

        if (currentTile == null) return false;

        if (currentTile instanceof GraveHolder graveHolder && graveHolder.getGraveStone() != null) {
            graveHolder.takeDamage(proj.getDamage(), projectileRow, projectileCol);
            GameSession.notify("grave in " + (projectileCol + 1) + " , " + (projectileRow + 1) + " take damage");
            proj.onHitObstacle(currentTile);
            return true;
        } else if (currentTile instanceof IceHolder iceHolder && iceHolder.hasIceBlock()) {
            iceHolder.takeIceDamage(proj.getDamage());
            proj.onHitObstacle(currentTile);
            return true;
        }

        return false;
    }

    private void checkZombiesAndZombiesCollision(Zombie z) {
        if (z.isDead()) return;

        int row = z.getRow();
        int targetCol = (int) ((z.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH + 0.2f);
        if (targetCol >= arena.getCols()) return;

        Tile targetTile = arena.getTile(row, targetCol);

        if (targetTile != null) {
            List<Zombie> zombiesToEat = arena.getZombiesOnTile(targetTile);
            Zombie targetZombie = null;

            for (Zombie enemyZ : zombiesToEat) {
                if (!enemyZ.isHypnotized() && !enemyZ.isDead()) {
                    targetZombie = enemyZ;
                    break;
                }
            }

            if (targetZombie != null) {
                if (!z.isAttacking()) {
                    z.setAttacking(true);
                }
            } else if (z.isAttacking()) {
                z.setAttacking(false);
            }
        }
    }

    private void checkZombiesAndPlantCollision(Zombie z) {
        if (z.isDead()) return;

        int row = z.getRow();
        int targetCol = z.getCol();

        Tile targetTile = arena.getTile(row, targetCol);

        if (targetCol >= 0) {
            handleZombieEat(z, targetTile);
        } else if (targetCol < 0) {
            LawnMower lawnMower = arena.getLawnMowers()[row];

            if (lawnMower != null && !lawnMower.isActivate()) return;
            else {
                Brain targetBrain = arena.getBrainInRow(row);

                if (targetBrain != null && !targetBrain.isEaten()) {
                    if (!z.isAttacking()) {
                        z.setAttacking(true);
                        z.setTile(null);
                    }
                    targetBrain.takeDamage(z.getEatDPS() / 10);
                } else {
                    if (z.isAttacking()) z.setAttacking(false);
                    if (z.getX() < PhysicalConstants.GRID_START_X - PhysicalConstants.TILE_WIDTH)
                        session.setZombieBreached(true);
                }
            }
        }
    }

    private void handleZombieEat(Zombie z, Tile targetTile) {
        List<Plant> plantToEat = targetTile.getPlants();
        Plant eatingPlant = null;
        if (!plantToEat.isEmpty()) {
            for (int i = plantToEat.size() - 1; i >= 0; i--) {
                Plant p = plantToEat.get(i);
                boolean canEat = true;

                for (IPlantStrategy strategy : p.getStrategies()) {
                    if (strategy instanceof TrapStrategy trap) {
                        if (trap.isArmed()) {
                            canEat = false;
                        }
                    }
                }

                if (canEat) {
                    eatingPlant = p;
                    break;
                }
            }
        }

        List<Zombie> zombiesToEat = arena.getZombiesOnTile(targetTile);
        Zombie targetZombie = null;

        for (Zombie zombie : zombiesToEat) {
            if (zombie.isHypnotized()) {
                targetZombie = zombie;
                break;
            }
        }

        if (eatingPlant != null) {
            if (!z.isAttacking()) {
                z.setAttacking(true);
            }
        } else if (targetZombie != null) {
            if (!z.isAttacking()) {
                z.setAttacking(true);
            }
        } else if (z.isAttacking()) {
            z.setAttacking(false);
        }
    }

    private void checkSunCollision(Sun sun) {
        if (sun.isCollected() && sun.isFalling() && sun.getType() == SunType.RADIOACTIVE_SUN) {
            Tile sunTile = arena.getTile(sun.getRow(), sun.getCol());
            int rightTile = Math.min(sunTile.getCol() + 2, arena.getCols() - 1);
            int leftTile = Math.max(sunTile.getCol() - 2, 0);
            int upTile = Math.min(sunTile.getRow() + 2, arena.getRows() - 1);
            int downTile = Math.max(sunTile.getRow() - 2, 0);

            List<Tile> affectedTiles = new ArrayList<>();

            for (int row = downTile; row <= upTile; row++)
                for (int col = leftTile; col <= rightTile; col++)
                    affectedTiles.add(arena.getTile(row, col));

            for (Zombie z : arena.getActiveZombies()) {
                Tile currentTile = arena.getTile(z.getRow(), z.getCol());
                if (z.isDead() || !affectedTiles.contains(currentTile)) continue;
                z.takeDamage(150);
                if (z.isDead()) {
                    GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
                        .zombie(z)
                        .seasonType(session.getCurrentChapter().getSeasonType())
                        .coordinate(z.getRow(), z.getCol())
                        .arena(arena)
                        .build();
                    GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED, payload);
                }
            }
            rightTile = Math.min(sunTile.getCol() + 1, arena.getCols() - 1);
            leftTile = Math.max(sunTile.getCol() - 1, 0);
            upTile = Math.min(sunTile.getRow() + 1, arena.getRows() - 1);
            downTile = Math.max(sunTile.getRow() - 1, 0);
            for (int row = downTile; row <= upTile; row++) {
                for (int col = leftTile; col <= rightTile; col++) {
                    List<Plant> tilePlants = arena.getTile(row, col).getPlants();
                    if (!tilePlants.isEmpty()) {
                        Plant damagePlant = tilePlants.getLast();
                        damagePlant.takeDamage(80);
                    }
                }
            }

            sun.setExploded(true);
        }
    }
}
