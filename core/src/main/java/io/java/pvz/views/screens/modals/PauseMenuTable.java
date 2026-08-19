package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.SettingController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.models.App;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

import java.util.function.Consumer;

import static com.badlogic.gdx.Gdx.app;

public class PauseMenuTable extends BorderedTable {

    private final SettingController controller = new SettingController();
    private int currentMusicVolume;
    private int currentSoundVolume;
    private Table blocker;

    public PauseMenuTable(Skin skin) {
        super();

        this.currentMusicVolume = (int) controller.getMusicVolume();
        this.currentSoundVolume = (int) controller.getSfxVolume();

        pad(30, 40, 30, 40);
        setSize(750, 600);

        buildContent(skin);
    }

    private void buildContent(Skin skin) {
        Table headerTable = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);

        Label titleLabel = new Label("GAME PAUSED", skin, "big");
        titleLabel.setColor(Color.valueOf("#4A3018"));
        titleLabel.setFontScale(1.8f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(45, 45).left();
        headerTable.add(titleLabel).expandX().center().padRight(45);

        add(headerTable).growX().padBottom(30).row();

        Table objectiveBox = new Table();

        Label.LabelStyle labelStyle = skin.get("bundle_reward_multiplier", Label.LabelStyle.class);
        if (labelStyle != null && labelStyle.background != null) {
            objectiveBox.setBackground(labelStyle.background);
        }

        Label objLabel = new Label("Don't let the zombies eat you brain!", skin);
        objLabel.setColor(Color.valueOf("#4A3018"));
        objLabel.setFontScale(1.4f);
        objLabel.setAlignment(Align.center);
        objLabel.setWrap(true);
        objectiveBox.add(objLabel).width(600).pad(20);

        add(objectiveBox).padBottom(30).row();

        Table slidersTable = new Table();
        Color fontColor = Color.valueOf("#4A3018");

        createSliderRow(slidersTable, skin, "Music", 0, 10, currentMusicVolume, 1.5f, fontColor, 25, val -> {
            currentMusicVolume = val;
            controller.setMusicVolume(val);
        });

        createSliderRow(slidersTable, skin, "Sound FX", 0, 10, currentSoundVolume, 1.5f, fontColor, 25, val -> {
            currentSoundVolume = val;
            controller.setSfxVolume(val);
        });

        add(slidersTable).padBottom(40).row();

        Table buttonsTable = new Table();

        TextButton saveExitBtn = UiFactory.textButton("SAVE AND EXIT", skin, "brown", 1.05f, 0.95f, () -> {
            System.out.println("Quitting to Plant Selection...");
            app.postRunnable(() -> {
                GameSession.destroyInstance();
                App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
                ScreenManager.getInstance().popScreen();
                ScreenManager.getInstance().popScreen();
            });
        });
        saveExitBtn.getLabel().setFontScale(1.1f);
        buttonsTable.add(saveExitBtn).width(200).height(65).padRight(15);

        TextButton restartBtn = UiFactory.textButton("RESTART", skin, "brown", 1.05f, 0.95f, () -> {
            System.out.println("Restarting Level...");
            this.remove();
        });
        restartBtn.getLabel().setFontScale(1.1f);
        buttonsTable.add(restartBtn).width(180).height(65).padRight(15);

        TextButton resumeBtn = UiFactory.textButton("RESUME", skin, "purple", 1.05f, 0.95f, this::remove);
        resumeBtn.getLabel().setFontScale(1.1f);
        buttonsTable.add(resumeBtn).width(180).height(65);

        add(buttonsTable).center();
    }

    private void createSliderRow(Table table, Skin skin, String title, float min, float max, int initialVal,
                                 float textScale, Color fontColor, float padBottom, Consumer<Integer> onChange) {
        Label label = new Label(title, skin);
        label.setColor(fontColor);
        label.setFontScale(textScale);

        Slider slider = new Slider(min, max, 1, false, skin);
        slider.setValue(initialVal);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onChange.accept((int) slider.getValue());
            }
        });

        table.add(label).left().padRight(30).padBottom(padBottom);
        table.add(slider).width(350).padBottom(padBottom).row();
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

        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );

        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        GameSession session = GameSession.getInstance();
        if (session != null)
            session.resumeGame();

        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
