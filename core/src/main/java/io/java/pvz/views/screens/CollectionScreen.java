package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.ScreenManager;

public class CollectionScreen extends BaseScreen {
    private final Skin skin;

    public CollectionScreen(Game game, Skin skin) {
        super(game);
        this.skin = skin;

        buildUI();
    }

    private void buildUI() {
        // ==========================================
        // ۱. بخش بالای صفحه (یک‌پنجم - ۲۰٪) کاملاً سیاه
        // ==========================================
        Table topTable = new Table();

        // دکمه خروج ساده متنی بدون اسکین
        Label closeBtn = new Label("X  CLOSE", skin);
        closeBtn.setColor(Color.WHITE);
        closeBtn.setFontScale(1.2f);

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Closing Collection...");
                ScreenManager.getInstance().popScreen();
            }
        });

        // قرار دادن دکمه در بالا و سمت راست
        topTable.add(closeBtn).expand().top().right().pad(25);

        // ==========================================
        // ۲. بخش پایین صفحه (چهار‌پنجم - ۸۰٪) با پس‌زمینه Skin
        // ==========================================
        Table bottomTable = new Table();

        // خواندن پس‌زمینه مستقیماً از Skin با اسمی که دادی
        bottomTable.setBackground(skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten"));

        Label placeholderLabel = new Label("Plant Collection Area", skin);
        placeholderLabel.setColor(Color.WHITE);
        bottomTable.add(placeholderLabel).center();

        // ==========================================
        // ۳. چیدمان دقیق در لایه اصلی
        // ==========================================

        // تنظیم ارتفاع به صورت درصدی از کل صفحه
        mainLayer.add(topTable).growX().height(Value.percentHeight(0.1f, mainLayer)).row();
        mainLayer.add(bottomTable).grow().height(Value.percentHeight(0.9f, mainLayer));
    }

    @Override
    public void render(float delta) {
        // رنگ کل صفحه را کاملاً سیاه می‌کنیم
        clearScreen(0f, 0f, 0f, 1f);

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
}
