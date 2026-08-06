package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.utils.Ids;
import pvz.libpvz.textures.TextureBank;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion backgroundRegion;

    public GameFlowScreen(Game game, String mapTextureId) {
        super(game);
        loadMap(mapTextureId);
        buildUI();
    }

    private void loadMap(String mapId) {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(mapId);
        if (backgroundRegion == null) {
            System.err.println("⚠️ Warning: Map texture not found for ID: " + mapId);
        }
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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (backgroundRegion != null) {
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        // TODO: draw entities in for loop

        batch.end();

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
}
