package io.java.pvz.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.views.screens.NewsModalTable;
import pvz.libpvz.textures.TextureBank;

public final class UiFactory {

    private UiFactory() {}

    public static Image imageFor(TextureBank textures, String imageId) {
        TextureRegion region = textures.region(imageId);
        if (region == null) {
            Gdx.app.error("UiFactory", "Missing image resource: " + imageId);
            return new Image();
        }
        return new Image(new TextureRegionDrawable(region));
    }

    public static Stack iconButton(TextureBank textures, Skin skin, String iconId, float width, float height,
                                   ButtonAnimator.OnClickListener clickListener) {
        Stack stack = new Stack();
        stack.setTransform(true);
        stack.setSize(width, height);
        stack.setTouchable(Touchable.enabled);

        Table bgTable = new Table();
        if (skin.has("image_ui_generic_brownbutton_10", Drawable.class)) {
            bgTable.setBackground(skin.getDrawable("image_ui_generic_brownbutton_10"));
        }
        Container<Table> bgContainer = new Container<>(bgTable);
        bgContainer.size(width, height);
        bgContainer.setTouchable(Touchable.disabled);

        Image icon = imageFor(textures, iconId);
        icon.setScaling(Scaling.fit);
        Container<Image> iconContainer = new Container<>(icon);
        iconContainer.size(width, height);
        iconContainer.setTouchable(Touchable.disabled);

        stack.add(bgContainer);
        stack.add(iconContainer);

        ButtonAnimator.applyHoverAndClickEffect(stack, 1.1f, 0.9f, clickListener);
        return stack;
    }

    public static TextButton getCloseBtn(Skin skin,Runnable onClose) {
        TextButton closeBtn = new TextButton("X", skin);
        closeBtn.getStyle().up = null;
        closeBtn.getStyle().down = null;
        Label btnLabel = closeBtn.getLabel();
        btnLabel.setColor(Color.BROWN);
        btnLabel.setFontScale(1.5f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    btnLabel.setFontScale(2f);
                }
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    btnLabel.setFontScale(1.5f);
                }
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(onClose != null){
                    onClose.run();
                }
            }
        });
        return closeBtn;
    }

}
