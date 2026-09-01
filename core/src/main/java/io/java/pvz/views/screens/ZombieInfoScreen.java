package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class ZombieInfoScreen extends BaseScreen {
    private final Skin skin;
    private final Zombie zombie;
    private TextureRegion backgroundRegion;

    public ZombieInfoScreen(Game game, Skin skin, Zombie zombie) {
        super(game);
        this.skin = skin;
        this.zombie = zombie;

        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        Label closeBtn = new Label("X", skin, "big");
        closeBtn.setColor(Color.WHITE);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.getInstance().popScreen();
            }
        });

        Table topBar = new Table();
        topBar.add(closeBtn).expandX().right().pad(20);
        rootTable.add(topBar).growX().row();
        Label titleLabel = new Label(zombie.getName(), skin, "big");
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).padBottom(20).row();
        Table contentTable = new Table();
        PamAnimatedActor zombieActor = PamAnimatedActor.createZombieIdle(zombie.getType());
        zombieActor.applyZombieArmor(zombie);
        contentTable.add(zombieActor).size(200, 200).expand().bottom().padBottom(300).padLeft(350);

        BorderedTable statsTable = new BorderedTable();
        statsTable.top();

        float cellPadding = 35f;
        float blockWidth = 250f;

        Label informationLabel = new Label("Description", skin, "big");
        informationLabel.setColor(Color.valueOf("#4A3018"));
        informationLabel.setFontScale(1.8f);
        informationLabel.setAlignment(Align.center);
        statsTable.add(informationLabel).colspan(2).padTop(50).padBottom(cellPadding * 2).row();

        float padX = 35f;
        float padY = 15f;
        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIETOUGHNESS_ICON",
            "TOUGHNESS", String.valueOf(zombie.getHealth()))).width(blockWidth).pad(padY, padX, padY, padX).left();
        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIESPEED_ICON",
            "SPEED", String.valueOf(zombie.getBaseSpeed()))).width(blockWidth).pad(padY, padX, padY, padX).left().row();

        contentTable.add(statsTable).size(850, 800).expand().top().right().padRight(50).padTop(20);

        rootTable.add(contentTable).grow();
        mainLayer.addActor(rootTable);
    }

    private Table createStatBlock(TextureBank textures, String iconKey, String title, String value) {
        Table block = new Table();
        block.left();

        Image icon = UiFactory.imageFor(textures, iconKey);
        if (icon != null) {
            block.add(icon).size(110, 110).padRight(15).left();
        }

        Table textTable = new Table();
        textTable.left();

        Label titleLbl = new Label(title, skin, "big");
        titleLbl.setFontScale(1f);
        titleLbl.setColor(Color.valueOf("#4A3018"));

        Label valueLbl = new Label(value, skin, "medium_outline");
        valueLbl.setFontScale(1.2f);
        valueLbl.setColor(Color.WHITE);

        textTable.add(titleLbl).left().row();
        textTable.add(valueLbl).left();

        block.add(textTable).expandX().left();

        return block;
    }

    @Override
    public void render(float delta) {
        clearScreen(0f, 0f, 0f, 1f);
        AssetLoader.getInstance().updateTextures();
        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }
        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }


}
