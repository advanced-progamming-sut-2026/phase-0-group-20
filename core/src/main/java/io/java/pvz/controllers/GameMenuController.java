package io.java.pvz.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class GameMenuController {

    private final Table modalLayer;
    private final Map<GameMenuType, Supplier<Table>> panelFactories = new EnumMap<>(GameMenuType.class);
    private GameMenuType current = GameMenuType.GAME_MENU;

    public GameMenuController(Table modalLayer) {
        this.modalLayer = modalLayer;
    }

    public void register(GameMenuType type, Supplier<Table> panelFactory) {
        panelFactories.put(type, panelFactory);
    }

    public void open(GameMenuType target) {
        if (target != current && !current.reachableMenus().contains(target)) {
            Gdx.app.error("GameMenuController", "Cannot navigate from " + current + " to " + target);
            return;
        }
        showPanel(target);
    }

    public void goBack() {
        GameMenuType exitTarget = current.getExitTarget();
        if (exitTarget != null) {
            showPanel(exitTarget);
        }
    }

    private void showPanel(GameMenuType target) {
        modalLayer.clearChildren();
        current = target;

        if (target == GameMenuType.GAME_MENU) {
            return;
        }

        Supplier<Table> factory = panelFactories.get(target);
        if (factory == null) {
            Gdx.app.error("GameMenuController", "No panel registered for " + target);
            return;
        }
        modalLayer.add(factory.get()).grow();
    }

    public GameMenuType getCurrent() {
        return current;
    }
}
