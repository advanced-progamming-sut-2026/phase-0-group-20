package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.DroppedSeedPacket;
import io.java.pvz.models.game.minigame.VaseBreakerLevel;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import java.util.*;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;
import static io.java.pvz.models.enums.PhysicalConstants.*;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion mainRegion;
    private TextureRegion leftRegion;
    private TextureRegion rightRegion;

    private Plant selectedPlantToPlace = null;
    private Image floatingPlantImage = null;

    private boolean isShovelSelected = false;
    private Image floatingShovelImage = null;

    private Image rowHighlight;
    private Image colHighlight;

    private ShapeRenderer shapeRenderer;
    private BitmapFont debugFont;

    private static final int COLS = 9;
    private static final int ROWS = 5;

    private String currentMapId;

    private boolean levelResultShown = false;

    private static final float TICK_DURATION = 1f / TimeManager.TICKS_PER_SECOND;
    private float simulationAccumulator = 0f;
    private BattlefieldRenderer battlefieldRenderer;

    private final GameFlowController gameFlowController = new GameFlowController();

    private final MiniGameController miniGameController = new MiniGameController();
    private DroppedSeedPacket selectedPacketToPlace = null;
    private final Map<DroppedSeedPacket, PlantCardButton> droppedPacketActors = new HashMap<>();
    private float visualWaveProgress = 0f;
    private ProgressBar waveProgressBar;
    private Image progressHeadIcon;

    public GameFlowScreen(Game game, String mapTextureId) {
        super(game);
        loadMap(mapTextureId);
        buildUI();

        shapeRenderer = new ShapeRenderer();
        debugFont = new BitmapFont();
        debugFont.getData().setScale(1.2f);

        camera.position.x = viewport.getWorldWidth();
        camera.update();
    }

    private void loadMap(String mainMapId) {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        this.currentMapId = mainMapId;
        mainRegion = textures.region(mainMapId);
        leftRegion = textures.region(mainMapId + "_LEFT");
        rightRegion = textures.region(mainMapId + "_RIGHT");

        if (mainRegion == null) System.err.println("⚠️ Warning: Map main texture not found: " + mainMapId);
        if (leftRegion == null) System.err.println("⚠️ Warning: Map left texture not found: " + mainMapId + "_LEFT");
        if (rightRegion == null) System.err.println("⚠️ Warning: Map right texture not found: " + mainMapId + "_RIGHT");
    }

    private void buildUI() {
        Skin skin = AssetLoader.getInstance().getSkin();
        TextureBank textures = AssetLoader.getInstance().getTextures();

        mainLayer.clear();
        mainLayer.setFillParent(true);

        battlefieldRenderer = new BattlefieldRenderer();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.35f);
        pixmap.fill();
        Texture highlightTex = new Texture(pixmap);
        pixmap.dispose();

        rowHighlight = new Image(highlightTex);
        colHighlight = new Image(highlightTex);
        rowHighlight.setVisible(false);
        colHighlight.setVisible(false);
        battlefieldRenderer.getHighlightLayer().addActor(rowHighlight);
        battlefieldRenderer.getHighlightLayer().addActor(colHighlight);

        mainLayer.addActor(battlefieldRenderer.getGroup());

        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) setupPlantSelectionMenu(skin, textures);
        setupIndicators(skin, textures);
        setupActionButtons(skin, textures);
        waveProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_green");
        waveProgressBar.setSize(400, 45);
        waveProgressBar.setPosition(1400f, 30f);
        waveProgressBar.setValue(0f);
        mainLayer.addActor(waveProgressBar);
        setupWaveMarkers(textures);
        progressHeadIcon = UiFactory.imageFor(textures, "IMAGE_UI_PERKS_RIFT_ICON_SAPMPLE_5");
        progressHeadIcon.setSize(55f, 55f);
        mainLayer.addActor(progressHeadIcon);
    }

    private void setupPlantSelectionMenu(Skin skin, TextureBank textures) {
        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin, () -> {
                buildSeedBank(skin, textures);
            });
            plantSelectionModal.show(modalLayer, viewport);
        } else {
            buildSeedBank(skin, textures);
        }
    }

    private void setupIndicators(Skin skin, TextureBank textures) {
        Stack sunStack = new Stack();
        sunStack.setSize(80, 80);
        sunStack.setPosition(100, 950);

        Image sunIcon = UiFactory.imageFor(textures, Ids.UI.SUN_ICON);
        sunStack.add(sunIcon);

        Label sunLabel = new Label("0", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (GameSession.getInstance() != null) {
                    setText(String.valueOf(GameSession.getInstance().getCurrentSun()));
                }
            }
        };
        sunLabel.setAlignment(Align.center);
        sunLabel.setFontScale(1.2f);
        sunLabel.setColor(Color.BLACK);
        sunStack.add(sunLabel);

        sunStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameFlowController.cheatAddSun("50");
            }
        });

        mainLayer.addActor(sunStack);

        Table plantFoodTable = new Table();
        plantFoodTable.setPosition(100, 850);
        plantFoodTable.left();

        Image plantFoodIcon = UiFactory.imageFor(textures, Ids.UI.PLANT_FOOD_ICON);

        Label plantFoodLabel = new Label("0", skin, "medium_outline") {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (App.getActiveUser() != null) {
                    setText("Count: " + String.valueOf(App.getActiveUser().getPlantFoodCount()));
                }
            }
        };
        plantFoodLabel.setFontScale(1f);
        plantFoodTable.add(plantFoodIcon).size(50, 40);
        plantFoodTable.add(plantFoodLabel).padLeft(15);

        mainLayer.addActor(plantFoodTable);
    }

    private void setupActionButtons(Skin skin, TextureBank textures) {
        Stack nukeBtn = UiFactory.iconButton(textures, skin, Ids.UI.NUKE_BUTTON, 150, 150, () -> {
            Result res = gameFlowController.releaseNuke();
            System.out.println(res);
        }, false);
        nukeBtn.setPosition(150, 150);
        mainLayer.addActor(nukeBtn);

        Stack shovelBtn = UiFactory.iconButton(textures, skin, Ids.UI.SHOVEL, 110, 110, () -> {
            if (floatingPlantImage != null) {
                floatingPlantImage.remove();
                floatingPlantImage = null;
                selectedPlantToPlace = null;
            }

            if (isShovelSelected) {
                isShovelSelected = false;
                if (floatingShovelImage != null) {
                    floatingShovelImage.remove();
                    floatingShovelImage = null;
                }
            } else {
                isShovelSelected = true;
                floatingShovelImage = new Image(UiFactory.imageFor(textures, Ids.UI.FLOATING_SHOVEL).getDrawable()) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(mousePos);
                        setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
                    }
                };
                floatingShovelImage.setSize(80, 80);
                floatingShovelImage.setTouchable(Touchable.disabled);
                mainLayer.addActor(floatingShovelImage);
            }
        }, false);
        shovelBtn.setPosition(1350, 855);
        mainLayer.addActor(shovelBtn);

        Stack pauseBtn = UiFactory.iconButton(textures, skin, Ids.UI.PAUSE, 90, 90, () -> {
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {
        });
        fastForwardBtn.setPosition(1620, 950);
        mainLayer.addActor(fastForwardBtn);
    }

    private void buildSeedBank(Skin skin, TextureBank textures) {
        Table seedBankTable = new Table();
        seedBankTable.setPosition(310f, 820f);
        seedBankTable.setSize(790f, 250f);
        seedBankTable.top().left();
        seedBankTable.pad(20f);

        List<Plant> selectedPlants = GameSession.getInstance().getChosenPlants();
        int count = 0;

        for (Plant plant : selectedPlants) {
            count++;
            PlantCardButton plantButton = createSeedPacket(plant, skin, textures);
            seedBankTable.add(plantButton).size(170f, 100f).padRight(15f).padBottom(20f);

            if (count % 4 == 0) {
                seedBankTable.row();
            }
        }
        mainLayer.addActor(seedBankTable);
    }

    private PlantCardButton createSeedPacket(Plant plant, Skin skin, TextureBank textures) {
        Image bgCard = UiFactory.imageFor(textures, Ids.PlantCards.BG_CARD);
        String plantName = UiFactory.getAtlasName(plant);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
        Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

        PlantCardButton plantButton = new PlantCardButton.Builder()
            .setBgImage(bgCard)
            .setPlant(plant)
            .setPlantImage(plantIcon)
            .setSkin(skin)
            .setShowProgressBar(false)
            .setShowLevel(false)
            .build();

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isShovelSelected) {
                    isShovelSelected = false;
                    if (floatingShovelImage != null) {
                        floatingShovelImage.remove();
                        floatingShovelImage = null;
                    }
                }
                if (floatingPlantImage != null) {
                    floatingPlantImage.remove();
                }

                selectedPlantToPlace = plant;

                floatingPlantImage = new Image(plantIcon.getDrawable()) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(mousePos);
                        setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
                    }
                };

                floatingPlantImage.setSize(80, 80);
                floatingPlantImage.setTouchable(Touchable.disabled);
                mainLayer.addActor(floatingPlantImage);
            }
        });

        return plantButton;
    }

    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);
        AssetLoader.getInstance().updateTextures();

        calculateProgressBar(delta);

        handleTileClick();
        handleShovelAction();
        handleCameraMovement(delta);
        updatePlantingHighlights();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawBackground();
        handleDebugSpawnKeys();
        batch.end();

        drawDebugLayout();
        advanceSimulation(delta);
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

        simulationAccumulator += delta;
        while (simulationAccumulator >= TICK_DURATION) {
            session.update(1);
            simulationAccumulator -= TICK_DURATION;
        }

        battlefieldRenderer.sync(session.getArena());

        GameState state = session.getState();
        if (!levelResultShown && (state == GameState.WON || state == GameState.LOST)) {
            gameFlowController.gameOver();
            levelResultShown = true;
            Skin skin = AssetLoader.getInstance().getSkin();
            new LevelResultTable(skin, state).show(modalLayer, viewport);
        }
    }

    private void handleDebugSpawnKeys() {
        if (GameSession.getInstance() == null) return;

        if (input.isKeyJustPressed(Input.Keys.P)) {
            gameFlowController.plantPlant("Peashooter", "1", "1");
        }

        if (input.isKeyJustPressed(Input.Keys.Z)) {
            gameFlowController.cheatSpawnZombie("Normal", "9", "1");
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

    private void handleTileClick() {
        if (GameSession.getInstance() == null) return;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (floatingPlantImage != null) {
                floatingPlantImage.remove();
                floatingPlantImage = null;
            }
            selectedPlantToPlace = null;
            selectedPacketToPlace = null;

            if (isShovelSelected && floatingShovelImage != null) {
                floatingShovelImage.remove();
                floatingShovelImage = null;
                isShovelSelected = false;
            }
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            float x = mousePos.x;
            float y = mousePos.y;

            if (x >= GRID_START_X && x <= GRID_START_X + (COLS * TILE_WIDTH) &&
                y >= GRID_START_Y && y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {

                int col = (int) ((x - GRID_START_X) / TILE_WIDTH) + 1;
                int row = (int) ((y - GRID_START_Y) / TILE_HEIGHT) + 1;

                if (isShovelSelected) {
                    Result result = gameFlowController.pluckPlant(String.valueOf(col), String.valueOf(row));
                    if (result.isSuccessful()) {
                        floatingShovelImage.remove();
                        floatingShovelImage = null;
                        isShovelSelected = false;
                    }
                    return;
                }

                if (selectedPlantToPlace != null && floatingPlantImage != null) {
                    Result result;
                    if (selectedPacketToPlace != null) {
                        result = miniGameController.plantFromVase(
                            String.valueOf(selectedPacketToPlace.getCol() + 1),
                            String.valueOf(selectedPacketToPlace.getRow() + 1),
                            String.valueOf(col), String.valueOf(row)
                        );
                    } else {
                        result = gameFlowController.plantPlant(selectedPlantToPlace.getName(),
                            String.valueOf(col), String.valueOf(row));
                    }

                    if (result.isSuccessful()) {
                        floatingPlantImage.remove();
                        floatingPlantImage = null;
                        selectedPlantToPlace = null;
                        selectedPacketToPlace = null;
                    }
                    return;
                }

                if (GameSession.getInstance().getCurrentMode() instanceof VaseBreakerLevel)
                    miniGameController.breakVase(String.valueOf(col), String.valueOf(row));
            }
        }
    }

    private void handleShovelAction() {
        if (!isShovelSelected || floatingShovelImage == null) return;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            floatingShovelImage.remove();
            floatingShovelImage = null;
            isShovelSelected = false;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            float x = mousePos.x;
            float y = mousePos.y;

            if (x >= GRID_START_X && x <= GRID_START_X + (COLS * TILE_WIDTH) &&
                y >= GRID_START_Y && y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {

                int col = (int) ((x - GRID_START_X) / TILE_WIDTH) + 1;
                int row = (int) ((y - GRID_START_Y) / TILE_HEIGHT) + 1;

                Result result = gameFlowController.pluckPlant(String.valueOf(col), String.valueOf(row));

                if (result.isSuccessful()) {
                    floatingShovelImage.remove();
                    floatingShovelImage = null;
                    isShovelSelected = false;
                }
            }
        }
    }

    private void syncDroppedPackets() {
        if (GameSession.getInstance() == null || GameSession.getInstance().getArena() == null) return;

        Skin skin = AssetLoader.getInstance().getSkin();
        TextureBank textures = AssetLoader.getInstance().getTextures();
        List<DroppedSeedPacket> packets = GameSession.getInstance().getArena().getDroppedSeedPackets();
        Set<DroppedSeedPacket> alivePackets = new HashSet<>(packets);

        for (DroppedSeedPacket packet : packets) {
            if (!droppedPacketActors.containsKey(packet) && !packet.isExpired()) {
                PlantCardButton card = createDroppedSeedPacket(packet, skin, textures);

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

    private PlantCardButton createDroppedSeedPacket(DroppedSeedPacket packet, Skin skin, TextureBank textures) {
        Plant plant = packet.getPlant();
        Image bgCard = UiFactory.imageFor(textures, Ids.PlantCards.BG_CARD);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + UiFactory.getAtlasName(plant).toUpperCase();
        Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

        PlantCardButton plantButton = new PlantCardButton.Builder()
            .setBgImage(bgCard)
            .setPlant(plant)
            .setPlantImage(plantIcon)
            .setSkin(skin)
            .setShowProgressBar(false)
            .setShowLevel(false)
            .build();

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float clickX, float clickY) {
                if (isShovelSelected) {
                    isShovelSelected = false;
                    if (floatingShovelImage != null) {
                        floatingShovelImage.remove();
                        floatingShovelImage = null;
                    }
                }

                if (floatingPlantImage != null) floatingPlantImage.remove();

                selectedPlantToPlace = plant;
                selectedPacketToPlace = packet;

                floatingPlantImage = new Image(plantIcon.getDrawable()) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(mousePos);
                        setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
                    }
                };
                floatingPlantImage.setSize(80, 80);
                floatingPlantImage.setTouchable(Touchable.disabled);
                mainLayer.addActor(floatingPlantImage);
            }
        });

        return plantButton;
    }

    private void updatePlantingHighlights() {
        if (isShovelSelected || ((selectedPlantToPlace != null || selectedPacketToPlace != null) && floatingPlantImage != null)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);
            float x = mousePos.x;
            float y = mousePos.y;

            if (x >= GRID_START_X && x <= GRID_START_X + (COLS * TILE_WIDTH) &&
                y >= GRID_START_Y && y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {

                int col = (int) ((x - GRID_START_X) / TILE_WIDTH);
                int row = (int) ((y - GRID_START_Y) / TILE_HEIGHT);

                rowHighlight.setSize(COLS * TILE_WIDTH, TILE_HEIGHT);
                rowHighlight.setPosition(GRID_START_X, GRID_START_Y + (row * TILE_HEIGHT));
                rowHighlight.setVisible(true);

                colHighlight.setSize(TILE_WIDTH, ROWS * TILE_HEIGHT);
                colHighlight.setPosition(GRID_START_X + (col * TILE_WIDTH), GRID_START_Y);
                colHighlight.setVisible(true);
                return;
            }
        }

        rowHighlight.setVisible(false);
        colHighlight.setVisible(false);
    }

    private void setupWaveMarkers(TextureBank textures) {
        if (waveProgressBar == null || GameSession.getInstance() == null) return;

        if (GameSession.getInstance().getCurrentMode() instanceof Level level) {
            int waveCount = level.getWaveCount();

            for (int i = 1; i <= waveCount; i++) {
                float fraction = (float) i / waveCount;
                Image flagImage = UiFactory.imageFor(textures,
                    "IMAGE_ZOMBIE_ZOMBIE_MODERN_VET_FLAG_ZOMBIE_MODERN_VET_FLAG_125X143"); //khabam miad bezar inja bashe baad dorosteshs mikonim

                flagImage.setSize(40f, 45f);
                float x = waveProgressBar.getX() + (waveProgressBar.getWidth() * fraction) - (flagImage.getWidth() / 2f);
                float y = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (flagImage.getHeight() / 2f);

                flagImage.setPosition(x, y);
                mainLayer.addActor(flagImage);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (debugFont != null) debugFont.dispose();
    }

    private void calculateProgressBar(float delta) {
        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof Level level) {
            float targetProgress = 0f;
            int waveCount = level.getWaveCount();

            if (waveCount > 0) {
                float waveSlice = 1f / waveCount;
                float baseProgress = (level.getCurrentWave() - 1) * waveSlice;
                float currentWaveProgress = 0f;
               Wave activeWave = GameSession.getInstance().getArena().getCurrentActiveWave();

                if (activeWave != null && activeWave.getTotalBaseHp() > 0) {
                    int currentHp = 0;
                    for (Zombie z : activeWave.getZombies())
                        if (!z.isDead())
                            currentHp += z.getHealth();

                    float destroyedFraction = 1f - ((float) currentHp / activeWave.getTotalBaseHp());

                    if (activeWave.isLastWave())
                        currentWaveProgress = destroyedFraction;
                    else
                        currentWaveProgress = Math.min(1f, destroyedFraction / 0.75f);

                }

                targetProgress = baseProgress + (currentWaveProgress * waveSlice);
                targetProgress = Math.max(0f, Math.min(1f, targetProgress));
            }

            visualWaveProgress += (targetProgress - visualWaveProgress) * delta * 1.5f;

            if (waveProgressBar != null)
                waveProgressBar.setValue(visualWaveProgress);


            if (progressHeadIcon != null && waveProgressBar != null) {
                float targetX = waveProgressBar.getX() + (waveProgressBar.getWidth() *
                    visualWaveProgress) - (progressHeadIcon.getWidth() / 2f);
                float targetY = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (progressHeadIcon.getHeight() / 2f);
                progressHeadIcon.setPosition(targetX, targetY);
            }
        }
    }

}
