package io.java.pvz.controllers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.NotificationToast;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements GameEventListener {

    private final Stage stage;
    private final Skin skin;
    private final List<NotificationToast> activeToasts = new ArrayList<>();
    private static final int MAX_NOTIFS = 3;

    public NotificationManager(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        GameEventMessenger.getInstance().addListener(GameEvent.NOTIFY, this);
    }

    public void register() {
        GameEventMessenger.getInstance().removeListener(GameEvent.NOTIFY, this);
        GameEventMessenger.getInstance().addListener(GameEvent.NOTIFY, this);
    }

    public void unregister() {
        GameEventMessenger.getInstance().removeListener(GameEvent.NOTIFY, this);
    }

    public void clearAllToasts() {
        for (NotificationToast toast : activeToasts) {
            toast.clearActions();
            toast.remove();
        }
        activeToasts.clear();
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.NOTIFY)
            showNotification(payload.getMessage());
    }

    public void showNotification(String message) {
        if (activeToasts.size() >= MAX_NOTIFS) {
            NotificationToast oldest = activeToasts.removeLast();
            oldest.dismiss();
        }

        NotificationToast newToast = new NotificationToast(message, skin, this);

        float startX = (stage.getViewport().getWorldWidth() - newToast.getWidth()) / 2f;
        float startY = stage.getViewport().getWorldHeight() + 50;
        newToast.setPosition(startX, startY);

        stage.addActor(newToast);
        activeToasts.addFirst(newToast);

        recalculatePositions();
    }

    public void removeToast(NotificationToast toast) {
        activeToasts.remove(toast);
        recalculatePositions();
    }

    private void recalculatePositions() {
        float targetY = stage.getViewport().getWorldHeight() - 100;
        float gap = 15f;

        for (NotificationToast toast : activeToasts) {
            toast.addAction(Actions.moveTo(toast.getX(), targetY, 0.4f, Interpolation.swingOut));
            targetY -= (toast.getHeight() + gap);
        }
    }

    public void dispose() {
        GameEventMessenger.getInstance().removeListener(GameEvent.NOTIFY, this);
    }
}
