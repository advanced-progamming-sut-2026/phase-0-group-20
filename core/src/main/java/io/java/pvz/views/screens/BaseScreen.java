package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.AudioManager;
import io.java.pvz.controllers.NotificationManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.views.sound.MusicType;

public abstract class BaseScreen implements Screen {

    protected final Game game;
    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected Stage stage;

    protected Stack rootStack;
    protected Table mainLayer;
    protected Table modalLayer;
    protected Table toastLayer;
    protected Table hudLayer;

    protected CurrencyBar currencyBar;
    private NotificationManager notificationManager;

    public BaseScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(1920, 1080, camera);
        stage = new Stage(viewport, batch);
        rootStack = new Stack();
        rootStack.setFillParent(true);

        mainLayer = new Table();
        modalLayer = new Table();
        toastLayer = new Table();
        hudLayer = new Table();
        hudLayer.setFillParent(true);
        hudLayer.top().left();

        rootStack.add(mainLayer);
        rootStack.add(modalLayer);
        rootStack.add(toastLayer);
        rootStack.add(hudLayer);

        stage.addActor(rootStack);

        if (showsCurrencyBar()) {
            currencyBar = new CurrencyBar(AssetLoader.getInstance().getTextures(), AssetLoader.getInstance().getSkin());
            hudLayer.add(currencyBar).padTop(40).padLeft(1200);
        }
        AudioManager.getInstance().playMusic(MusicType.MENU_BGM);
        getNotificationManager();
    }

    protected boolean showsCurrencyBar() {
        return true;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        if (notificationManager != null)
            notificationManager.register();

    }

    protected void clearScreen(float r, float g, float b, float a) {
        Gdx.gl.glClearColor(r, g, b, a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void render(float delta) {
        clearScreen(0.15f, 0.15f, 0.2f, 1f);

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    public void setupButton(TextButton button, Runnable clickAction) {
        button.setTransform(true);
        button.setOrigin(Align.center);

        button.addListener(new ClickListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                button.addAction(Actions.scaleTo(1.15f, 1.15f, 0.1f, Interpolation.sineOut));
                //we should play hover sound.
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f, Interpolation.sineOut));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                //we should play click sound.
                if (clickAction != null) {
                    clickAction.run();
                }
            }
        });
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null && AssetLoader.getInstance().getSkin() != null) {
            notificationManager = new NotificationManager(stage, AssetLoader.getInstance().getSkin());
        }
        return notificationManager;
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
        camera.update();
        if (batch != null)
            batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == stage) {
            Gdx.input.setInputProcessor(null);
        }
        if (notificationManager != null) {
            notificationManager.clearAllToasts();
            notificationManager.unregister();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();
        if (notificationManager != null) notificationManager.dispose();

    }
}
