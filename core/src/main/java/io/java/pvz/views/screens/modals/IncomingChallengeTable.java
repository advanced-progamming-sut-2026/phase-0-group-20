package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.MatchmakingController;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class IncomingChallengeTable extends BorderedTable {

    private Table blocker;

    public IncomingChallengeTable(Skin skin, String inviteId, String fromUsername) {
        super();
        pad(35);
        setSize(700, 340);

        Label title = new Label("Multiplayer", skin, "big");
        title.setColor(Color.valueOf("#4A3018"));
        title.setFontScale(1.5f);
        title.setAlignment(Align.center);
        add(title).padBottom(20).row();

        Label message = new Label(fromUsername + "invited you to challenge. Do you Accept?",
            skin);
        message.setColor(Color.valueOf("#4A3018"));
        message.setFontScale(1.25f);
        message.setAlignment(Align.center);
        message.setWrap(true);
        add(message).width(620).padBottom(30).row();

        Table buttonsTable = new Table();

        TextButton declineBtn = UiFactory.textButton("Denied", skin, "brown", 1.05f, 0.95f, () -> {
            respond(inviteId, false, buttonsTable);
        });
        declineBtn.getLabel().setFontScale(1.15f);
        buttonsTable.add(declineBtn).width(220).height(70).padRight(20);

        TextButton acceptBtn = UiFactory.textButton("Accept", skin, "purple", 1.05f, 0.95f, () -> {
            respond(inviteId, true, buttonsTable);
        });
        acceptBtn.getLabel().setFontScale(1.15f);
        buttonsTable.add(acceptBtn).width(220).height(70);

        add(buttonsTable).center();
    }

    private void respond(String inviteId, boolean accepted, Table buttonsTable) {
        buttonsTable.setTouchable(Touchable.disabled);
        MatchmakingController.getInstance().respondToChallenge(inviteId, accepted, response -> {
            if (response == null || !response.isSuccess()) {
                notify("Not invalid Challenge Request: " + response);
            }
            remove();
        });
    }

    private void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY).message(message).build());
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        blocker = new Table();
        blocker.setSize(width, height);
        blocker.setTouchable(Touchable.enabled);
        blocker.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        targetLayer.addActor(blocker);

        setPosition(Math.round((width - getWidth()) / 2f), Math.round((height - getHeight()) / 2f));
        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) blocker.remove();
        return super.remove();
    }
}
