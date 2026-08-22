package io.java.pvz.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.java.pvz.models.enums.Menu;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class MenuScreenController {

    private final Table modalLayer;
    private final Map<Menu, Supplier<Table>> panelFactories = new EnumMap<>(Menu.class);
    private Menu current = Menu.GAME_MENU;

    public MenuScreenController(Table modalLayer) {
        this.modalLayer = modalLayer;
    }

    public void register(Menu type, Supplier<Table> panelFactory) {
        panelFactories.put(type, panelFactory);
    }

    public void open(Menu target) {
        System.out.println("current: " + current + " target: " + target);
        if (target != current && !current.getAllowedEntryTargets().contains(target)) {
            Gdx.app.error("GameMenuController", "Cannot navigate from " + current + " to " + target);
            return;
        }
        showPanel(target);
    }

    public void goBack() {
        Menu exitTarget = current.getExitTarget();
        if (exitTarget != null) {
            showPanel(exitTarget);
        }
    }

    private void showPanel(Menu target) {
        modalLayer.clearChildren();
        current = target;

        if (target == Menu.GAME_MENU) {
            return;
        }

        Supplier<Table> factory = panelFactories.get(target);
        if (factory == null) {
            Gdx.app.error("GameMenuController", "No panel registered for " + target);
            return;
        }
        modalLayer.add(factory.get()).grow();
    }

    public Menu getCurrent() {
        return current;
    }
}
