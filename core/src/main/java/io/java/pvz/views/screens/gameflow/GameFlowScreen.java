package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.MatchController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.events.*;
import io.java.pvz.models.game.minigame.DroppedSeedPacket;
import io.java.pvz.models.timeManager.TimeManager;

import io.java.pvz.utils.DialogueLine;
import io.java.pvz.models.game.events.CameraListener;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.minigame.DroppedSeedPacket;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.BaseScreen;
import io.java.pvz.views.screens.modals.DialogueModalTable;
import io.java.pvz.views.screens.modals.LevelResultTable;
import pvz.libpvz.textures.TextureBank;

import java.util.*;

import static com.badlogic.gdx.Gdx.input;
import static io.java.pvz.models.enums.PhysicalConstants.*;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion mainRegion;
    private TextureRegion leftRegion;
    private TextureRegion rightRegion;
    private ShapeRenderer shapeRenderer;
    private BitmapFont debugFont;
    private static final int COLS = 9;
    private static final int ROWS = 5;

    private String currentMapId;
    private CameraListener cameraListener;

    private boolean levelResultShown = false;

    private static final float TICK_DURATION = 1f / TimeManager.TICKS_PER_SECOND;
    private float simulationAccumulator = 0f;
    private BattlefieldRenderer battlefieldRenderer;

    private final GameFlowController gameFlowController = new GameFlowController();
    private final MiniGameController miniGameController = new MiniGameController();

    private final Map<DroppedSeedPacket, PlantCardButton> droppedPacketActors = new HashMap<>();

    private GameHUD gameHUD;
    private GameInputHandler inputHandler;

    public GameFlowScreen(Game game, String mapTextureId) {
        super(game);
        loadMap(mapTextureId);

        mainLayer.clear();
        mainLayer.setFillParent(true);

        battlefieldRenderer = new BattlefieldRenderer();
        mainLayer.addActor(battlefieldRenderer.getGroup());

        inputHandler = new GameInputHandler(mainLayer, viewport, battlefieldRenderer.getHighlightLayer(),
            gameFlowController, miniGameController);
        gameHUD = new GameHUD(mainLayer, modalLayer, viewport, inputHandler, gameFlowController);
        inputHandler.setGameHUD(gameHUD);

        gameHUD.buildUI();

        shapeRenderer = new ShapeRenderer();
        debugFont = new BitmapFont();
        debugFont.getData().setScale(1.2f);

        camera.position.x = viewport.getWorldWidth();
        camera.update();

        cameraListener = new CameraListener(camera, stage);
        GameEventMessenger.getInstance().addListener(GameEvent.GARGANTUAR_MOVES, cameraListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_EXPLODED, cameraListener);
        GameEventMessenger.getInstance().addListener(GameEvent.LAWNMOWER_TRIGGERED, cameraListener);
        GameEventMessenger.getInstance().addListener(GameEvent.GO_DISPLAYED, cameraListener);
        GameEventMessenger.getInstance().addListener(GameEvent.RELEASED_NUKE, cameraListener);

        MatchController.getInstance().setOnMatchEnd(message -> {
            String winnerRoleStr = message.getString("winnerRole");
            String reason = message.getString("reason");
            io.java.pvz.net.server.PlayerRole myRole = MatchController.getInstance().getCurrentRole();

            GameState finalState = GameState.LOST;
            if (myRole != null && myRole.name().equals(winnerRoleStr)) {
                finalState = GameState.WON;
            }

            if (!levelResultShown) {
                levelResultShown = true;
                if (GameSession.getInstance() != null)
                    GameSession.getInstance().pauseGame();
                LevelResultTable resultTable = new LevelResultTable(AssetLoader.getInstance().getSkin(),
                    finalState, game);
                resultTable.show(modalLayer, viewport);
                GameSession.notify("Match Ended: " + reason);
            }
        });
    }

    private void loadMap(String mainMapId) {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        this.currentMapId = mainMapId;
        mainRegion = textures.region(mainMapId);
        leftRegion = textures.region(mainMapId + "_LEFT");
        rightRegion = textures.region(mainMapId + "_RIGHT");

        if (mainRegion == null) System.err.println("Warning: Map main texture not found: " + mainMapId);
        if (leftRegion == null) System.err.println("Warning: Map left texture not found: " + mainMapId + "_LEFT");
        if (rightRegion == null) System.err.println("Warning: Map right texture not found: " + mainMapId + "_RIGHT");
    }

    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);
        AssetLoader.getInstance().updateTextures();
        GameSession session = GameSession.getInstance();
        float simDelta = delta;

        if (session != null) {
            if (session.getState() == GameState.PAUSED)
                simDelta = 0f;
            else
                simDelta = delta * session.getSpeedMultiplier();
            simDelta*= App.getSettings().getProgressSpeed();
        }
        gameHUD.update(simDelta);
        inputHandler.handleTileClick();
        inputHandler.handleCouchPlayKeyboard();
        inputHandler.sickAnimations();
        handleCameraMovement(delta);
        inputHandler.updatePlantingHighlights();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawBackground();
        batch.end();

        if (App.getSettings().isGrid())
            drawDebugLayout();
        advanceSimulation(simDelta);
        syncDroppedPackets();

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void handleCameraMovement(float delta) {
        camera.update();
        float screenH = viewport.getWorldHeight();
        float leftDrawW = leftRegion.getRegionWidth() * (screenH / leftRegion.getRegionHeight());
        float mainDrawW = mainRegion.getRegionWidth() * (screenH / mainRegion.getRegionHeight());
        float rightDrawW = rightRegion.getRegionWidth() * (screenH / rightRegion.getRegionHeight());

        if (currentMapId != null && currentMapId.toLowerCase().contains("ice")) {
            mainDrawW += 25;
        }

        if (input.isKeyPressed(Input.Keys.RIGHT) &&
            camera.position.x < leftDrawW + rightDrawW + mainDrawW - viewport.getWorldWidth() / 2f)
            camera.position.x += 300 * delta;
        if (input.isKeyPressed(Input.Keys.LEFT) && camera.position.x > viewport.getWorldWidth() / 2f)
            camera.position.x -= 300 * delta;
    }

    private void drawBackground() {
        float screenH = viewport.getWorldHeight();
        float currentX = 0;

        float leftScale = screenH / leftRegion.getRegionHeight();
        float leftDrawW = leftRegion.getRegionWidth() * leftScale;
        batch.draw(leftRegion, currentX, 0, leftDrawW, screenH);
        currentX += leftDrawW;

        float mainScale = screenH / mainRegion.getRegionHeight();
        float mainDrawW = mainRegion.getRegionWidth() * mainScale;
        float mainDrawY = 0f;
        float mainDrawH = screenH;

        if (currentMapId != null && currentMapId.toLowerCase().contains("ice")) {
            mainDrawY = -25;
            mainDrawH = screenH + 25;
            mainDrawW += 25;
        }

        batch.draw(mainRegion, currentX, mainDrawY, mainDrawW, mainDrawH);
        currentX += mainDrawW;

        float rightScale = screenH / rightRegion.getRegionHeight();
        float rightDrawW = rightRegion.getRegionWidth() * rightScale;
        batch.draw(rightRegion, currentX, 0, rightDrawW, screenH);
    }

    private void advanceSimulation(float delta) {
        GameSession session = GameSession.getInstance();
        if (session == null) return;

        if (session.getState() != GameState.PAUSED && !session.isGameOver()) {
            simulationAccumulator += delta;
            while (simulationAccumulator >= TICK_DURATION) {
                gameFlowController.advanceTime(1);
                simulationAccumulator -= TICK_DURATION;
            }
        }

        battlefieldRenderer.sync(session.getArena());

        GameState state = session.getState();
        if (!levelResultShown && (state == GameState.WON || state == GameState.LOST)) {
            gameFlowController.gameOver();
            levelResultShown = true;
            new LevelResultTable(AssetLoader.getInstance().getSkin(), state, game).show(modalLayer, viewport);
        }
    }

    private void drawDebugLayout() {
        drawDebugShapes();
        drawDebugText();
    }

    private void drawDebugShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            shapeRenderer.line(vx, GRID_START_Y, vx, GRID_START_Y + (ROWS * TILE_HEIGHT));
        }
        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            shapeRenderer.line(GRID_START_X, hy, GRID_START_X + (COLS * TILE_WIDTH), hy);
        }
        shapeRenderer.setColor(Color.RED);
        float mowerWidth = 100f;
        float mowerX = GRID_START_X - mowerWidth - 30;
        for (int i = 0; i < ROWS; i++) {
            float mowerY = GRID_START_Y + (i * TILE_HEIGHT) + 20;
            shapeRenderer.rect(mowerX, mowerY, mowerWidth, TILE_HEIGHT - 40);
        }
        shapeRenderer.setColor(Color.PURPLE);
        float zombieSpawnX = GRID_START_X + (COLS * TILE_WIDTH) + 100;
        shapeRenderer.rect(zombieSpawnX, GRID_START_Y, 700, ROWS * TILE_HEIGHT);

        for (int i = 0; i < ROWS; i++) {
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            shapeRenderer.line(zombieSpawnX + 50, firstColY, zombieSpawnX + 600, firstColY);
        }
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(30, 950, 180, 80);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(30, 850, 220, 80);

        shapeRenderer.setColor(Color.BROWN);
        float bankX = 310f, bankY = 820;
        shapeRenderer.rect(bankX, bankY, 790, 250);
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 4; c++) {
                shapeRenderer.rect(bankX + 20 + (c * 185), bankY + 20 + (r * 120), 170, 100);
            }
        }

        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(1400f, 30f, 400, 45);
        shapeRenderer.rect(150, 150, 150, 150);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(1350f, 855f, 110, 110);
        shapeRenderer.setColor(Color.MAGENTA);
        shapeRenderer.rect(1620f, 950f, 250, 90);
        shapeRenderer.end();
    }

    private void drawDebugText() {
        batch.begin();
        debugFont.setColor(Color.WHITE);

        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            debugFont.draw(batch, "X:" + (int) vx, vx - 15, GRID_START_Y + (ROWS * TILE_HEIGHT) + 30);
        }

        float zombieSpawnX = GRID_START_X + (COLS * TILE_WIDTH) + 100;
        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            debugFont.draw(batch, "Y:" + (int) hy, GRID_START_X - 60, hy + 5);
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            if (i < ROWS) {
                debugFont.draw(batch, "Zombie Rows Y\n(Y:" + (int) firstColY + ")" +
                    "\n from x = " + (zombieSpawnX + 20)
                    + " to x = " + (zombieSpawnX + 620), zombieSpawnX + 20, firstColY);
            }
        }

        float mowerX = GRID_START_X - 100f - 30;
        debugFont.draw(batch, "Mowers\nX:" + (int) mowerX, mowerX, GRID_START_Y - 20);
        debugFont.draw(batch, "Sun\n(30, 950)", 40, 1010);
        debugFont.draw(batch, "PlantFood\n(30, 850)", 40, 910);
        debugFont.draw(batch, "Seed Bank (X:310 , Y:820)", 310f, 810f);
        debugFont.draw(batch, "Wave Progress\n(X:1400 , Y:30)", 1400f, 110f);
        debugFont.draw(batch, "release the nuke\n(X:150, Y:150)", 150f, 135f);
        debugFont.draw(batch, "Shovel\n(X:1350, Y:855)", 1350f, 840f);
        debugFont.draw(batch, "Controls (Pause/FF)\n(X:1620, Y:950)", 1620f, 935f);

        batch.end();
    }

    private void syncDroppedPackets() {
        if (GameSession.getInstance() == null || GameSession.getInstance().getArena() == null) return;

        TextureBank textures = AssetLoader.getInstance().getTextures();
        List<DroppedSeedPacket> packets = GameSession.getInstance().getArena().getDroppedSeedPackets();
        Set<DroppedSeedPacket> alivePackets = new HashSet<>(packets);

        for (DroppedSeedPacket packet : packets) {
            if (!droppedPacketActors.containsKey(packet) && !packet.isExpired()) {
                PlantCardButton card = createDroppedSeedPacket(packet, textures);

                float targetW = 100f;
                float targetH = 65f;
                float x = GRID_START_X + (packet.getCol() * TILE_WIDTH) + (TILE_WIDTH / 2f) - (targetW / 2f);
                float y = GRID_START_Y + (packet.getRow() * TILE_HEIGHT) + (TILE_HEIGHT / 2f) - (targetH / 2f);

                card.setSize(targetW, targetH);
                card.setPosition(x, y);
                mainLayer.addActor(card);
                droppedPacketActors.put(packet, card);
            }
        }

        Iterator<Map.Entry<DroppedSeedPacket, PlantCardButton>> it = droppedPacketActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<DroppedSeedPacket, PlantCardButton> entry = it.next();
            if (!alivePackets.contains(entry.getKey()) || entry.getKey().isExpired()) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private PlantCardButton createDroppedSeedPacket(DroppedSeedPacket packet, TextureBank textures) {
        Plant plant = packet.getPlant();
        Image bgCard = UiFactory.imageFor(textures, Ids.PlantCards.BG_CARD);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + UiFactory.getAtlasName(plant).toUpperCase();
        Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

        PlantCardButton plantButton = new PlantCardButton.Builder()
            .setBgImage(bgCard)
            .setPlant(plant)
            .setPlantImage(plantIcon)
            .setSkin(AssetLoader.getInstance().getSkin())
            .setShowProgressBar(false)
            .setShowLevel(false)
            .build();

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float clickX, float clickY) {
                inputHandler.onDroppedPacketClicked(plant, packet, plantIcon);
            }
        });

        return plantButton;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (debugFont != null) debugFont.dispose();
        GameEventMessenger.getInstance().removeListener(GameEvent.WAVE_STARTED_PLAYTIME,gameHUD.getAnnounceListener());
        GameEventMessenger.getInstance().removeListener(GameEvent.FINAL_WAVE_STARTED, gameHUD.getAnnounceListener());
        GameEventMessenger.getInstance().removeListener(GameEvent.GARGANTUAR_MOVES, cameraListener);
        GameEventMessenger.getInstance().removeListener(GameEvent.PLANT_EXPLODED, cameraListener);
        GameEventMessenger.getInstance().removeListener(GameEvent.LAWNMOWER_TRIGGERED, cameraListener);
        GameEventMessenger.getInstance().removeListener(GameEvent.GO_DISPLAYED, cameraListener);
        GameEventMessenger.getInstance().removeListener(GameEvent.RELEASED_NUKE, cameraListener);
    }
}
