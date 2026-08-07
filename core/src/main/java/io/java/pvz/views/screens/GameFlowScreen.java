package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.enums.Menu;
import pvz.libpvz.textures.TextureBank;

import static com.badlogic.gdx.Gdx.input;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion mainRegion;
    private TextureRegion leftRegion;
    private TextureRegion rightRegion;

    public GameFlowScreen(Game game, String mapTextureId) {
        super(game);
        loadMap(mapTextureId);
        buildUI();

        camera.position.x = viewport.getWorldWidth();
        camera.update();
    }

    private void loadMap(String mainMapId) {
        TextureBank textures = AssetLoader.getInstance().getTextures();

        mainRegion = textures.region(mainMapId);
        leftRegion = textures.region(mainMapId + "_LEFT");//the address has a _LEFT or a _RIGHT on its last part
        rightRegion = textures.region(mainMapId + "_RIGHT");

        if (mainRegion == null) System.err.println("⚠️ Warning: Map main texture not found: " + mainMapId);
        if (leftRegion == null) System.err.println("⚠️ Warning: Map left texture not found: " + mainMapId + "_LEFT");
        if (rightRegion == null) System.err.println("⚠️ Warning: Map right texture not found: " + mainMapId + "_RIGHT");
    }

    private void buildUI() {
        Skin skin = AssetLoader.getInstance().getSkin();

        mainLayer.clear();
        mainLayer.setFillParent(true);

        // TODO: pause and other options
        Label tempLabel = new Label("Game UI Layer (Seed Bank goes here)", skin);
        tempLabel.setAlignment(Align.center);
        mainLayer.add(tempLabel).expand().top().padTop(20);

        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin);
            plantSelectionModal.show(modalLayer, viewport);
        }
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
        batch.draw(mainRegion, currentX, 0, mainDrawW, screenH);
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

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
}
