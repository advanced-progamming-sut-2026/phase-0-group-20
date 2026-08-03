package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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

    public BaseScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(1920, 1080, camera);
    }

    @Override
    public void show() {
        stage = new Stage(viewport, batch);

        rootStack = new Stack();
        rootStack.setFillParent(true);

        mainLayer = new Table();
        modalLayer = new Table();
        toastLayer = new Table();

        rootStack.add(mainLayer);
        rootStack.add(modalLayer);
        rootStack.add(toastLayer);

        stage.addActor(rootStack);
        Gdx.input.setInputProcessor(stage);
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

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
