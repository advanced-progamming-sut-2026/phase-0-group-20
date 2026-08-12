package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.views.GameMenu;
import io.java.pvz.views.screens.GameFlowScreen;
import pvz.skin.BorderedTable;

public class ScoreLevelModal extends BorderedTable {

    private Actor blocker;
    private final Game game;

    public ScoreLevelModal(Skin skin,Game game) {
        super();
        this.game = game;
        setSize(550, 400);
        top();
        pad(30);

        Label titleLabel = new Label("Score Level", skin, "big");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.valueOf("#4A3018"));

        Label questionLabel = new Label("Do You Want to Start a Score Game ?", skin, "big");
        questionLabel.setAlignment(Align.center);
        questionLabel.setColor(Color.BROWN);

        Label highScoreLabel = new Label("High Score : "+ App.getActiveUser().getHighestBonusScore(), skin, "medium");
        highScoreLabel.setAlignment(Align.center);
        highScoreLabel.setColor(Color.valueOf("#4A3018"));

        Table buttonTable = new Table();
        TextButton playBtn = new TextButton("Play", skin, "green");
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Result result = new GameMenuController().enterScoringLevel();
                if(result.isSuccessful()){
                    String mapId = new GameMenuController().getCurrentMapTextureId();
                    ScreenManager.getInstance().pushScreen(new GameFlowScreen(game, mapId));
                }
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(result.message())
                        .build());
            }
        });
        buttonTable.add(playBtn).size(160, 65).center();

        Label backBtn = new Label("X", skin, "big");
        backBtn.setColor(Color.BROWN);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });

        Table headerTable = new Table();
        headerTable.add(backBtn).left().width(40);
        headerTable.add(titleLabel).expandX().center();
        headerTable.add().width(40);

        add(headerTable).growX().padBottom(40).row();
        add(questionLabel).growX().padBottom(30).row();
        add(highScoreLabel).growX().expandY().top().padBottom(20).row();
        add(buttonTable).padBottom(10).bottom();
    }

    public void show(Group modalLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);

        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        this.blocker = blockerTable;
        modalLayer.addActor(blocker);

        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );
        modalLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
