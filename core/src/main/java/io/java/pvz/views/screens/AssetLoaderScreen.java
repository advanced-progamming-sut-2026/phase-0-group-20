package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.GameInitializer;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.game.events.AudioListener;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.users.User;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.client.ServerConfig;
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

    private boolean isTransitioning = false;

    public AssetLoaderScreen(Game game) {
        super(game);

        AssetLoader.getInstance().init();
        Skin skin = AssetLoader.getInstance().getSkin();
        addListeners();
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

        if (!isInitFinished && virtualProgress < 0.9f) virtualProgress += delta * 0.2f;
        else if (isInitFinished && virtualProgress < 1f) virtualProgress += delta / 2;
        progressBar.setValue(virtualProgress);


        if (isInitFinished && virtualProgress >= 1f && !isTransitioning) {
            isTransitioning = true;
            User stayedUser = DataBaseManager.getLoggedInUser();
            GameEventMessenger.getInstance().dispatch(GameEvent.ENTERED_MENUS,
                new GameEventPayload.Builder(GameEvent.ENTERED_MENUS).build());
            if (stayedUser == null) {
                App.setActiveAdventure(new Adventure());
                ScreenManager.getInstance().setRootScreen(new SignupScreen(game));
            } else {

                new Thread(() -> {
                   try {
                       if (!NetworkClient.getInstance().isConnected()) {
                           NetworkClient.getInstance().connect(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT);
                       }

                       NetworkController.getInstance().login(
                           stayedUser.getUsername(),
                           stayedUser.getPassword(),
                           true,
                           response -> {
                               Gdx.app.postRunnable(() -> {
                                   if (response != null && response.isSuccess()) {
                                       App.setActiveUser(stayedUser);
                                       App.setActiveMenu(Menu.MAIN_MENU);
                                       App.setActiveAdventure(new Adventure());
                                       App.setAllUsers(DataBaseManager.getAllUsers());
                                       ScreenManager.getInstance().setRootScreen(new MainMenuScreen(game));
                                   } else {
                                       String error = response != null ? response.getErrorMessage() : "Error";
                                       ScreenManager.getInstance().setRootScreen(new SignupScreen(game));
                                       GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                                           new GameEventPayload.Builder(GameEvent.NOTIFY).message("Error in auto login: " + error).build());
                                   }
                               });
                           }
                       );
                   } catch (Exception e) {
                       Gdx.app.postRunnable(() -> {
                           ScreenManager.getInstance().setRootScreen(new LoginScreen(game));
                           GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                               new GameEventPayload.Builder(GameEvent.NOTIFY).message("Server is Not Run").build());
                       });
                   }
                }).start();
            }
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }

    private void addListeners() {
        AudioListener audioListener = new AudioListener();
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_MENUS, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_EGYPT, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_FROZEN_CAVES, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_ZEN_GARDEN, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_DARK_AGES, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ENTERED_BIG_WAVE_BEACH, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.LAWNMOWER_TRIGGERED, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_PLACED, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PROJECTILE_HIT, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_TAKING_DAMAGE, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_LOST, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.PROJECTILE_FIRED, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBOSS_TALKS, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.DAVE_TALKS, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBOSS_PHASE_1, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBOSS_PHASE_2, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBOSS_PHASE_3, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.LEVEL_COMPLETED, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.GAME_OVER, audioListener);
        GameEventMessenger.getInstance().addListener(GameEvent.WAVE_STARTED_PLAYTIME, audioListener);
    }

}
