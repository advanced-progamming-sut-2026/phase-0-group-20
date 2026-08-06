package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.GameInitializer;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import pvz.libpvz.textures.TextureBank;

public class AssetLoaderScreen extends BaseScreen {

    private final ProgressBar progressBar;
    private final Image logoImage;

    private TextureRegion backgroundRegion;
    private TextureRegion logoRegion;

    private boolean isInitStarted = false;
    private boolean isInitFinished = false;
    private float virtualProgress = 0f;

    public AssetLoaderScreen(Game game) {
        super(game);

        AssetLoader.getInstance().init();
        Skin skin = AssetLoader.getInstance().getSkin();

        mainLayer.clear();
        mainLayer.setFillParent(true);

        logoImage = new Image();
        logoImage.setScaling(Scaling.fit);

        Label loadingLabel = new Label("Loading Game Assets...", skin);
        loadingLabel.setAlignment(Align.center);

        progressBar = new ProgressBar(0f, 1f, 0.01f, false, skin, "xp_green");
        progressBar.setValue(0f);

        mainLayer.add(logoImage).size(700, 350).padBottom(40).row();
        mainLayer.add(loadingLabel).padBottom(15).row();
        mainLayer.add(progressBar).width(800).height(45).center();
    }

    @Override
    public void render(float delta) {
        clearScreen(0.08f, 0.08f, 0.12f, 1f);

        AssetLoader.getInstance().updateTextures();
        TextureBank textures = AssetLoader.getInstance().getTextures();

        if (backgroundRegion == null)
            backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        if (logoRegion == null) {
            logoRegion = textures.region(Ids.MainMenu.LOGO);
            if (logoRegion != null)
                logoImage.setDrawable(new TextureRegionDrawable(logoRegion));
        }

        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        if (!isInitStarted) {
            isInitStarted = true;
            new Thread(() -> {
                GameInitializer.loadAllResources();
                isInitFinished = true;
            }).start();
        }

        if (!isInitFinished && virtualProgress < 0.9f) virtualProgress += delta * 0.1f;
        else if (isInitFinished && virtualProgress < 1f) virtualProgress += delta;
        progressBar.setValue(virtualProgress);

        if (isInitFinished && virtualProgress >= 1f) {
            User stayedUser = DataBaseManager.getLoggedInUser();
            if (stayedUser == null) {
                App.setActiveAdventure(new Adventure());
                ScreenManager.getInstance().setRootScreen(new SignupScreen(game));
            } else {
                App.setActiveUser(stayedUser);
                App.setActiveMenu(Menu.MAIN_MENU);
                App.setActiveAdventure(new Adventure());
                System.out.println("Welcome back, " + stayedUser.getUsername() + "!");
                App.setAllUsers(DataBaseManager.getAllUsers());
                ScreenManager.getInstance().setRootScreen(new MainMenuScreen(game));
            }
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }
}
