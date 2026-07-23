package models.game.minigame;

import models.App;
import models.Position;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

import java.util.Random;

public class BeghouledManager {

    public String swapPlants(int r1, int c1, int r2, int c2) {
        GameSession session = GameSession.getInstance();

        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) {
            return "Invalid move: Tiles are not adjacent!";
        }

        Tile tile1 = session.getArena().getTile(r1, c1);
        Tile tile2 = session.getArena().getTile(r2, c2);

        if (tile1.isCrater() || tile2.isCrater()) {
            return "Invalid move: Cannot swap with a crater!";
        }

        Plant p1 = tile1.getPlants().isEmpty() ? null : tile1.getPlants().get(0);
        Plant p2 = tile2.getPlants().isEmpty() ? null : tile2.getPlants().get(0);

        forceSwap(tile1, tile2, p1, p2);

        int matchScore = checkForMatchesAndRemove(session, false);

        if (matchScore > 0) {

            ((BeghouledLevel) session.getCurrentMode()).addSuccessfulMatch();

            int sunGained = 50 + (matchScore > 3 ? (matchScore - 3) * 50 : 0); // more suns for bigger match
            session.addSun(sunGained);

            applyGravity(session);

            return "Match found! You gained " + sunGained + " suns.";
        } else {
            forceSwap(tile1, tile2, p2, p1);
            return "Invalid move: No match-3 created!";
        }
    }

    private void forceSwap(Tile t1, Tile t2, Plant p1, Plant p2) {
        t1.getPlants().clear();
        t2.getPlants().clear();
        if (p2 != null) t1.addPlant(p2);
        if (p1 != null) t2.addPlant(p1);
    }

    private void applyGravity(GameSession session) {
        boolean movedAny = dropPlantsAndFill(session);

        if (movedAny) {
            handleCascades(session);
        }
    }

    private boolean dropPlantsAndFill(GameSession session) {
        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();
        boolean movedAny = false;

        for (int c = 0; c < cols; c++) {
            for (int r = rows - 1; r >= 0; r--) {
                Tile targetTile = session.getArena().getTile(r, c);
                if (targetTile.isCrater()) continue;

                if (targetTile.getPlants().isEmpty()) {
                    if (pullPlantFromAbove(session, r, c, targetTile)) {
                        movedAny = true;
                    } else if (spawnRandomPlant(session, targetTile)) {
                        movedAny = true;
                    }
                }
            }
        }
        return movedAny;
    }

    private boolean pullPlantFromAbove(GameSession session, int r, int c, Tile targetTile) {
        for (int k = r - 1; k >= 0; k--) {
            Tile upperTile = session.getArena().getTile(k, c);

            if (!upperTile.isCrater() && !upperTile.getPlants().isEmpty()) {
                Plant fallingPlant = upperTile.getPlants().get(0);

                upperTile.getPlants().clear();
                targetTile.addPlant(fallingPlant);
                fallingPlant.setPosition(new Position(targetTile.getCol(), targetTile.getRow()));

                return true;
            }
        }
        return false;
    }

    private boolean spawnRandomPlant(GameSession session, Tile targetTile) {
        String[] basePlants = {"peashooter", "sunflower", "wall-nut", "snow pea", "cabbage-pult"};
        Random random = new Random();
        String randomPlantName = basePlants[random.nextInt(basePlants.length)];
        Plant template = App.findPlantByName(randomPlantName);

        if (template != null) {
            Plant newPlant = PlantFactory.create(template.getId());
            targetTile.addPlant(newPlant);
            session.getArena().addPlant(newPlant);
            session.getTimeManager().registerNewTicker(newPlant);
            return true;
        }
        return false;
    }

    private void handleCascades(GameSession session) {
        int cascadeScore = checkForMatchesAndRemove(session, true);

        if (cascadeScore > 0) {
            ((BeghouledLevel) session.getCurrentMode()).addSuccessfulMatch();

            int sunGained = 50 + (cascadeScore > 3 ? (cascadeScore - 3) * 50 : 0);
            sunGained += 50;
            session.addSun(sunGained);

            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message("Cascade Match! You gained bonus " + sunGained + " suns.")
                            .build());

            applyGravity(session);
        }
    }

    private int checkForMatchesAndRemove(GameSession session, boolean isCascade) {
        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();
        boolean[][] matched = new boolean[rows][cols];

        flagHorizontalMatches(session, matched, rows, cols);
        flagVerticalMatches(session, matched, rows, cols);

        return removeMatchedPlants(session, matched, rows, cols);
    }

    private void flagHorizontalMatches(GameSession session, boolean[][] matched, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols - 3; c++) {
                Plant p1 = getPlantAt(session, r, c);
                Plant p2 = getPlantAt(session, r, c + 1);
                Plant p3 = getPlantAt(session, r, c + 2);

                if (p1 != null && p2 != null && p3 != null) {
                    if (p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName())) {
                        matched[r][c] = true;
                        matched[r][c + 1] = true;
                        matched[r][c + 2] = true;
                    }
                }
            }
        }
    }

    private void flagVerticalMatches(GameSession session, boolean[][] matched, int rows, int cols) {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - 3; r++) {
                Plant p1 = getPlantAt(session, r, c);
                Plant p2 = getPlantAt(session, r + 1, c);
                Plant p3 = getPlantAt(session, r + 2, c);

                if (p1 != null && p2 != null && p3 != null) {
                    if (p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName())) {
                        matched[r][c] = true;
                        matched[r + 1][c] = true;
                        matched[r + 2][c] = true;
                    }
                }
            }
        }
    }

    private int removeMatchedPlants(GameSession session, boolean[][] matched, int rows, int cols) {
        int matchCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (matched[r][c]) {
                    matchCount++;
                    Tile tile = session.getArena().getTile(r, c);
                    Plant p = tile.getPlants().get(0);

                    session.getTimeManager().unregisterTicker(p);
                    tile.getPlants().clear();
                }
            }
        }
        return matchCount;
    }

    private Plant getPlantAt(GameSession session, int r, int c) {
        Tile tile = session.getArena().getTile(r, c);
        if (tile == null || tile.isCrater() || tile.getPlants().isEmpty()) return null;
        return tile.getPlants().get(0);
    }

}
