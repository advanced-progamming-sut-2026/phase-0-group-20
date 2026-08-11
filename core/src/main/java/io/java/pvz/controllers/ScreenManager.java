package io.java.pvz.controllers;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import io.java.pvz.models.App;
import io.java.pvz.models.database.DataBaseManager;

import java.util.Stack;

public class ScreenManager {
    private static ScreenManager instance;
    private Game game;

    private Stack<Screen> screenStack;

    private ScreenManager() {
        screenStack = new Stack<>();
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void initialize(Game game) {
        this.game = game;
    }

    public void pushScreen(Screen newScreen) {
        if (game == null) return;

        if (!screenStack.isEmpty()) {
            Screen current = screenStack.peek();
            current.pause();
        }

        screenStack.push(newScreen);
        game.setScreen(newScreen);
    }

    public void popScreen() {
        if (game == null || screenStack.isEmpty()) return;

        Screen current = screenStack.pop();
        current.dispose();
        if(App.getActiveUser() != null)
            DataBaseManager.saveOrUpdateUser(App.getActiveUser());
        if (!screenStack.isEmpty()) {
            Screen previous = screenStack.peek();
            previous.resume();
            game.setScreen(previous);
        } else {
            Gdx.app.exit();
        }
    }

    public void setRootScreen(Screen rootScreen) {
        if (game == null) return;

        while (!screenStack.isEmpty()) {
            Screen s = screenStack.pop();
            s.dispose();
        }
        screenStack.push(rootScreen);
        game.setScreen(rootScreen);
    }

    public Screen getCurrentScreen() {
        if (screenStack.isEmpty()) return null;
        return screenStack.peek();
    }

    public Game getGame() {
        return game;
    }
}
