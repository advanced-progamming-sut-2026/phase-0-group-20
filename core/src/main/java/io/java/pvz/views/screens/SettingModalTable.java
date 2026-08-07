package io.java.pvz.views.screens;

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
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

import java.util.function.Consumer;

public class SettingModalTable extends BorderedTable {
    private final SettingController controller = new SettingController();
    private int currentMusicVolume;
    private int currentSoundVolume;
    private int currentDifficulty;
    private boolean isGrid;
    private boolean isDebug;
    private Actor blocker;

    public SettingModalTable(Skin skin) {
        super();

        this.currentMusicVolume = (int) controller.getMusicVolume();
        this.currentSoundVolume = (int) controller.getSfxVolume();
        this.currentDifficulty = controller.getDifficulty();
        this.isGrid = controller.isGrid();
        this.isDebug = controller.isDebug();

        pad(45, 40, 40, 40);
        buildSettings(skin);
        setSize(700, 800);
    }

    private void buildSettings(Skin skin) {
        Table headerTable = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);

        Label titleLabel = new Label("Settings", skin);
        titleLabel.setColor(Color.valueOf("#4A3018"));
        titleLabel.setFontScale(2.5f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(50, 50).left();
        headerTable.add(titleLabel).expandX().center().padRight(50);

        add(headerTable).growX().padBottom(50).row();

        Table contentTable = new Table();
        contentTable.top();

        float textScale = 1.6f;
        Color fontColor = Color.valueOf("#4A3018");

        createSliderRow(contentTable, skin, "Music Volume", 0, 10, currentMusicVolume,
            textScale, fontColor, 35, val -> {
            currentMusicVolume = val;
            controller.setMusicVolume(val);
        });
        createSliderRow(contentTable, skin, "SFX Volume", 0, 10, currentSoundVolume,
            textScale, fontColor, 35, val -> {
            currentSoundVolume = val;
            controller.setSfxVolume(val);
        });
        createSliderRow(contentTable, skin, "Difficulty", 1, 5, currentDifficulty,
            textScale, fontColor, 55, val -> {
            currentDifficulty = val;
            controller.setDifficulty(val);
        });

        CheckBox gridCheckBox = createCheckBox(skin, " Show Grid", isGrid, textScale, fontColor, val -> {
            isGrid = val;
            controller.setGrid(val);
        });
        CheckBox debugCheckBox = createCheckBox(skin, " Debug Mode", isDebug, textScale, fontColor, val -> {
            isDebug = val;
            controller.setDebug(val);
        });

        contentTable.add(gridCheckBox).colspan(3).left().padBottom(25).row();
        contentTable.add(debugCheckBox).colspan(3).left().row();

        add(contentTable).expand().top();
    }

    private void createSliderRow(Table table, Skin skin, String title, float min, float max, int initialVal,
                                 float textScale, Color fontColor, float padBottom, Consumer<Integer> onChange) {
        Label label = new Label(title, skin);
        label.setColor(fontColor);
        label.setFontScale(textScale);

        Slider slider = new Slider(min, max, 1, false, skin);
        slider.setValue(initialVal);

        Label valueLabel = new Label(String.valueOf(initialVal), skin);
        valueLabel.setColor(fontColor);
        valueLabel.setFontScale(textScale);

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) slider.getValue();
                valueLabel.setText(String.valueOf(val));
                onChange.accept(val);
            }
        });

        table.add(label).left().padRight(40).padBottom(padBottom);
        table.add(slider).width(300).padBottom(padBottom);
        table.add(valueLabel).width(60).left().padLeft(20).padBottom(padBottom).row();
    }

    private CheckBox createCheckBox(Skin skin, String text, boolean initialVal, float textScale, Color fontColor,
                                    Consumer<Boolean> onChange) {
        CheckBox checkBox = new CheckBox(text, skin, "default");
        checkBox.getLabel().setColor(fontColor);
        checkBox.getLabel().setFontScale(textScale);
        checkBox.setChecked(initialVal);
        checkBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onChange.accept(checkBox.isChecked());
            }
        });
        return checkBox;
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
                return true;
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
