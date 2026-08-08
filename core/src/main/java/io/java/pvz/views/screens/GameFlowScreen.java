package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import static com.badlogic.gdx.Gdx.input;
import static io.java.pvz.models.enums.PhysicalConstants.*;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion mainRegion;
    private TextureRegion leftRegion;
    private TextureRegion rightRegion;

    private ShapeRenderer shapeRenderer;
    private BitmapFont debugFont;

    private final int COLS = 9;
    private final int ROWS = 5;

    private String currentMapId;

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
        leftRegion = textures.region(mainMapId + "_LEFT");//the address has a _LEFT or a _RIGHT on its last part
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

        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin);
            plantSelectionModal.show(modalLayer, viewport);
        }

        Image sunIcon = UiFactory.imageFor(textures, Ids.UI.SUN_ICON);
        sunIcon.setSize(80, 80);
        sunIcon.setPosition(100, 950);
        mainLayer.addActor(sunIcon);

        Image plantFoodIcon = UiFactory.imageFor(textures, Ids.UI.PLANT_FOOD_ICON);
        plantFoodIcon.setSize(100, 80);
        plantFoodIcon.setPosition(100, 850);
        mainLayer.addActor(plantFoodIcon);

        Stack nukeBtn = UiFactory.iconButton(textures, skin, Ids.UI.NUKE_BUTTON, 150, 150, () -> {
            System.out.println("Nuke launched!");
        }, false);
        nukeBtn.setPosition(150, 150);
        mainLayer.addActor(nukeBtn);

        Stack shovelBtn = UiFactory.iconButton(textures, skin, Ids.UI.SHOVEL, 110, 110, () -> {
            System.out.println("Shovel picked up!");
        }, false);
        shovelBtn.setPosition(1350, 855);
        mainLayer.addActor(shovelBtn);


        Stack pauseBtn = UiFactory.iconButton(textures, skin, Ids.UI.PAUSE, 90, 90, () -> {
            System.out.println("Game Paused");
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {
            System.out.println("Speed toggled!");
        });
        fastForwardBtn.setPosition(1620, 950);
        mainLayer.addActor(fastForwardBtn);
    }

    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);

        AssetLoader.getInstance().updateTextures();

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

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


        //for debugging and seeing whole map
        if (input.isKeyPressed(Input.Keys.RIGHT) &&
            camera.position.x < leftDrawW + rightDrawW + mainDrawW - viewport.getWorldWidth() / 2f)
            camera.position.x += 300 * delta;

        if (input.isKeyPressed(Input.Keys.LEFT) && camera.position.x > viewport.getWorldWidth() / 2f)
            camera.position.x -= 300 * delta;


        batch.end();

        drawDebugLayout();

        if (input.isKeyPressed(Input.Keys.RIGHT)) camera.position.x += 600 * delta;
        if (input.isKeyPressed(Input.Keys.LEFT)) camera.position.x -= 600 * delta;

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void drawDebugLayout() {
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
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 4; c++)
                shapeRenderer.rect(bankX + 20 + (c * 185), bankY + 20 + (r * 120), 170, 100);


        shapeRenderer.setColor(Color.ORANGE);
        float progX = 1400f, progY = 30f;
        shapeRenderer.rect(progX, progY, 400, 45);

        float nukX = 150;
        shapeRenderer.rect(nukX, nukX, nukX, nukX);

        shapeRenderer.setColor(Color.BLUE);
        float shovelX = 1350f, shovelY = 855f;
        shapeRenderer.rect(shovelX, shovelY, 110, 110);

        shapeRenderer.setColor(Color.MAGENTA);
        float controlsX = 1620f, controlsY = 950f;
        shapeRenderer.rect(controlsX, controlsY, 250, 90);

        shapeRenderer.end();

        batch.begin();
        debugFont.setColor(Color.WHITE);

        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            debugFont.draw(batch, "X:" + (int) vx, vx - 15, GRID_START_Y + (ROWS * TILE_HEIGHT) + 30);
        }

        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            debugFont.draw(batch, "Y:" + (int) hy, GRID_START_X - 60, hy + 5);
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            if (i < ROWS)
                debugFont.draw(batch, "Zombie Rows Y\n(Y:" + (int) firstColY + ")" + "\n from x = " + (zombieSpawnX + 20)
                    + " to x = " + (zombieSpawnX + 620), zombieSpawnX + 20, firstColY);
        }

        debugFont.draw(batch, "Mowers\nX:" + (int) mowerX, mowerX, GRID_START_Y - 20);


        debugFont.draw(batch, "Sun\n(30, 950)", 40, 1010);
        debugFont.draw(batch, "PlantFood\n(30, 850)", 40, 910);
        debugFont.draw(batch, "Seed Bank (X:" + (int) bankX + " , Y:" + (int) bankY + ")", bankX, bankY - 10);
        debugFont.draw(batch, "Wave Progress\n(X:" + (int) progX + " , Y:" + (int) progY + ")", progX, progY + 80);
        debugFont.draw(batch, "release the nuke\n(X:" + (int) nukX + ", Y:" + (int) nukX + ")", nukX, nukX - 15);

        debugFont.draw(batch, "Shovel\n(X:" + (int) shovelX + ", Y:" + (int) shovelY + ")", shovelX, shovelY - 15);
        debugFont.draw(batch, "Controls (Pause/FF)\n(X:" + (int) controlsX + ", Y:" + (int) controlsY + ")",
            controlsX, controlsY - 15);

        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (debugFont != null) debugFont.dispose();
    }
}
