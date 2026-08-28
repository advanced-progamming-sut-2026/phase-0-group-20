package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.MatchController;
import io.java.pvz.models.game.GameSession;
import org.jspecify.annotations.NonNull;
import pvz.skin.BorderedTable;

public class LevelIntroModalTable extends BorderedTable {

    private Actor blocker;
    private final boolean isOnlineMatch;

    public LevelIntroModalTable(Skin skin) {
        super();
        this.setTouchable(Touchable.enabled);

        this.isOnlineMatch = MatchController.getInstance().isOnlineMatch();

        pad(40, 50, 40, 50);

        buildContent(skin);

        setSize(600, 450);
    }

    private void buildContent(Skin skin) {
        Table contentTable = new Table();

        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() != null) {
            String modeDetails = GameSession.getInstance().getCurrentMode().toString();
            String[] parts = modeDetails.split("-");

            for (String part : parts) {
                Label label = createTitleLabel(skin, part);
                contentTable.add(label).padBottom(15f).row();
            }
        }
        if (isOnlineMatch) {
            Label readyLabel = new Label("GET READY...", skin, "big");
            readyLabel.setColor(Color.valueOf("#2ECC71"));
            readyLabel.setFontScale(1.5f);
            readyLabel.setAlignment(Align.center);
            contentTable.add(readyLabel).padTop(30f).row();

            this.add(contentTable).center();

            this.addAction(Actions.sequence(
                Actions.delay(4.0f),
                Actions.run(() -> {
                    remove();
                    if (GameSession.getInstance() != null) {
                        GameSession.getInstance().resumeGame();
                    }
                })
            ));
        } else {
            TextButton startBtn = new TextButton("LET'S ROCK!", skin,"green");
            startBtn.getLabel().setFontScale(1.2f);
            startBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    remove();
                    if (GameSession.getInstance() != null) {
                        GameSession.getInstance().resumeGame();
                        if(GameSession.getInstance() != null){
                            GameSession.getInstance().playTheme(GameSession.getInstance().getCurrentMode());
                        }
                    }
                }
            });

            contentTable.add(startBtn).size(250f, 70f).padTop(30f);
            this.add(contentTable).center();
        }
    }

    private static @NonNull Label createTitleLabel(Skin skin, String part) {
        Label label = new Label(part.trim(), skin, "bundle_reward_multiplier");
        label.setFontScale(1.5f);
        label.setAlignment(Align.center);
        label.setColor(Color.WHITE);
        return label;
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);

        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return isOnlineMatch;
            }
        });

        this.blocker = blockerTable;
        targetLayer.addActor(blocker);

        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );

        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
