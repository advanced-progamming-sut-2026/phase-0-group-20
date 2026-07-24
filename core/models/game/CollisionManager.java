package models.game;

import models.entities.Sun;
import models.entities.SunType;
import models.entities.obstacle.PushableObstacle;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.enums.plants.ProjectileType;
import models.fields.Brain;
import models.fields.LawnMower;
import models.fields.obstacle.GraveHolder;
import models.fields.obstacle.IceHolder;
import models.fields.tiles.Tile;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;

public class CollisionManager {
    private final GameSession session;
    private final Arena arena;

    public CollisionManager(GameSession session) {
        this.session = session;
        this.arena = session.getArena();
    }

    public void checkAllCollisions() {
        List<Zombie> activeZombies = arena.getActiveZombies();

        // for projectiles
        for (Projectile proj : arena.getActiveProjectiles()) {
            if (proj.isDestroyed()) continue;

            Tile currentTile = arena.getTile(proj.getPosition().getRow(), proj.getPosition().getCol());

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

            if (proj.isFiredByZombie())
                checkProjectileForPlantCollision(proj);
            else {
                boolean hitObstacle = checkProjectileForObstaclesCollision(proj);
                if (!hitObstacle && !proj.isDestroyed())
                    checkProjectileForZombieCollision(proj);
            }
        }

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

    private void checkProjectileForPlantCollision(Projectile projectile) {
        int row = projectile.getPosition().getRow();
        int col = projectile.getPosition().getCol();
        if (col < 0 || col >= arena.getCols()) return;

        Tile tile = arena.getTile(row, col);
        if (tile == null || tile.getPlants().isEmpty()) return;

        List<Plant> plantsHere = tile.getPlants();
        Plant target = plantsHere.get(plantsHere.size() - 1);

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


        int tileLength = PhysicalConstants.TILE_UNIT_LENGTH;
        float projectileHitRadius = 0.25f;
        float zombieHitRadius = 0.25f;

        float physProjectileRadius = projectileHitRadius * tileLength;
        float physZombieRadius = zombieHitRadius * tileLength;

        int bottomRow = (int) Math.floor((projectile.getY() - physProjectileRadius) / tileLength);
        int topRow = (int) Math.floor((projectile.getY() + physProjectileRadius) / tileLength);

        bottomRow = Math.max(0, bottomRow);
        topRow = Math.min(arena.getRows() - 1, topRow);

        List<Zombie> nearbyZombies = new ArrayList<>();
        for (int row = bottomRow; row <= topRow; row++)
            nearbyZombies.addAll(arena.zombieInRow(row));

        float combinedRadius = physProjectileRadius + physZombieRadius;

        for (Zombie z : nearbyZombies) {
            if (z.isDead()) continue;

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
        int projectileCol = (int) (proj.getX() / PhysicalConstants.TILE_UNIT_LENGTH);
        Tile currentTile = arena.getTile(projectileRow, projectileCol);

        if (currentTile == null) return false;

        if (currentTile instanceof GraveHolder graveHolder && graveHolder.getGraveStone() != null) {
            graveHolder.takeDamage(proj.getDamage(), projectileRow, projectileCol);
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
        int targetCol = (int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - 0.2);

        Tile targetTile = arena.getTile(row, targetCol);

        if (targetTile != null) {
            List<Zombie> zombiesToEat = arena.getZombiesOnTile(targetTile);
            Zombie targetZombie = null;
            if (!zombiesToEat.isEmpty()) {
                targetZombie = zombiesToEat.get(0);
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
        int targetCol = (int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - 0.2);

        Tile targetTile = arena.getTile(row, targetCol);

        if (targetTile != null) {
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
                    if (z.getX() < -PhysicalConstants.TILE_UNIT_LENGTH) session.setZombieBreached(true);
                }
            }
        }
    }

    private void handleZombieEat(Zombie z, Tile targetTile) {
        List<Plant> plantToEat = targetTile.getPlants();
        Plant eatingPlant = null;
        if (!plantToEat.isEmpty()) {
            eatingPlant = plantToEat.get(plantToEat.size() - 1);
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