package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.SettingController;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class SettingModalTable extends BorderedTable {
    private final SettingController controller = new SettingController();
    public SettingModalTable(Skin skin) {
        super();
        pad(45, 40, 40, 40);
        buildSettings(skin);
        setSize(650, 75);
    }

    private void buildSettings(Skin skin) {
        Table headerTable = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);
        Label titleLabel = new Label("Settings", skin);
        titleLabel.setColor(Color.valueOf("#4A3018"));
        titleLabel.setFontScale(1.3f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(45, 45).left();
        headerTable.add(titleLabel).expandX().center();

        add(headerTable).growX().padBottom(40).row();


        Table contentTable = new Table();
        contentTable.top();

        Label musicLabel = new Label("Music Volume", skin);
        musicLabel.setColor(Color.valueOf("#4A3018"));
        Slider musicSlider = new Slider(0, 10, 1, false, skin);

        Label sfxLabel = new Label("SFX Volume", skin);
        sfxLabel.setColor(Color.valueOf("#4A3018"));
        Slider sfxSlider = new Slider(0, 10, 1, false, skin);

        Label difficultyLabel = new Label("Difficulty", skin);
        difficultyLabel.setColor(Color.valueOf("#4A3018"));
        Slider difficultySlider = new Slider(0, 5, 1, false, skin);

        CheckBox gridCheckBox = new CheckBox(" Show Grid", skin,"default");
        gridCheckBox.getLabel().setColor(Color.valueOf("#4A3018"));

        CheckBox debugCheckBox = new CheckBox(" Debug Mode", skin , "default");
        debugCheckBox.getLabel().setColor(Color.valueOf("#4A3018"));

        contentTable.add(musicLabel).left().padRight(30).padBottom(25);
        contentTable.add(musicSlider).width(250).padBottom(25).row();

        contentTable.add(sfxLabel).left().padRight(30).padBottom(25);
        contentTable.add(sfxSlider).width(250).padBottom(25).row();

        contentTable.add(difficultyLabel).left().padRight(30).padBottom(45);
        contentTable.add(difficultySlider).width(250).padBottom(45).row();

        contentTable.add(gridCheckBox).colspan(2).left().padBottom(15).row();
        contentTable.add(debugCheckBox).colspan(2).left().row();

        add(contentTable).expand().top();
    }

}
