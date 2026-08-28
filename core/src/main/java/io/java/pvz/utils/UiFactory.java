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
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import pvz.libpvz.textures.TextureBank;

public final class UiFactory {

    private UiFactory() {
    }

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
        return iconButton(textures, skin, iconId, width, height, clickListener, true);
    }

    public static Stack iconButton(TextureBank textures, Skin skin, String iconId, float width, float height,
                                   ButtonAnimator.OnClickListener clickListener, boolean hasBackground) {
        Stack stack = new Stack();
        stack.setTransform(true);
        stack.setSize(width, height);
        stack.setTouchable(Touchable.enabled);

        Table bgTable = new Table();
        if (hasBackground && skin.has("image_ui_generic_brownbutton_10", Drawable.class)) {
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

    public static TextButton getCloseBtn(Skin skin, Runnable onClose) {
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
                if (onClose != null) {
                    onClose.run();
                }
            }
        });
        return closeBtn;
    }

    public static TextButton textButton(String text, Skin skin, String styleName, float hoverScale, float clickScale,
                                        ButtonAnimator.OnClickListener clickListener) {
        TextButton button = new TextButton(text, skin, styleName);
        if (clickListener != null)
            ButtonAnimator.applyHoverAndClickEffect(button, hoverScale, clickScale, clickListener);
        return button;
    }

    public static Image imageButton(Skin skin, String drawableName, float hoverScale, float clickScale,
                                    ButtonAnimator.OnClickListener clickListener) {
        Image image = new Image(skin.getDrawable(drawableName));
        image.setOrigin(Align.center);
        image.setTouchable(Touchable.enabled);
        if (clickListener != null)
            ButtonAnimator.applyHoverAndClickEffect(image, hoverScale, clickScale, clickListener);
        return image;
    }

    public static String getAtlasName(Plant plant) {
        String name = plant.getName();
        return switch (name) {
            case "Rotobaga" -> "XSHOT";
            case "Goo Peashooter" -> "POISONPEASHOOTER";
            case "Mega Gatling Pea" -> "MEGAGATLING";
            case "Cherry Bomb" -> "CHERRY_BOMB";
            case "Iceberg Lettuce" -> "ICEBURG";
            case "Pierce-mint" -> "SPEARMINT";
            case "Cat-tail" -> "HOMINGTHISTLE";
            case "catTail-mint" -> "CONTAINMINT";
            default -> name.replace("-", "").replace(" ", "");
        };
    }

    public static String getAnimationName(Plant plant) {
        String name = plant.getName();
        return switch (name) {
            case "Rotobaga" -> "ROTORUTABAGA";
            case "Mega Gatling Pea" -> "MEGAGATLING";
            case "Iceberg Lettuce" -> "ICEBURG";
            case "Twin Sunflower" -> "SUNFLOWER_TWIN";
            case "Primal Sunflower" -> "PRIMAL_SUNFLOWER";
            case "Primal Potato Mine" -> "PRIMAL_POTATOMINE";
            case "Primal Peashooter" -> "PRIMAL_PEASHOOTER";
            case "Kernel-pult" -> "KERNALPULT";
            case "Phat Beet" -> "PHATBEETS";
            case "Pierce-mint" -> "SPEARMINT";
            case "Cat-tail" -> "HOMINGTHISTLE";
            case "catTail-mint" -> "CONTAINMINT";

            default -> name.replace("-", "").replace(" ", "").toUpperCase();
        };
    }

    public static String getZombieAddress(Zombie zombie) {
        ZombieType zombieType = zombie.getType();
        return switch (zombieType) {
            case NORMAL -> "TUTORIAL";
            case CONE -> "MUMMY_ARMOR1";
            case BUCKET -> "MUMMY_ARMOR2";
            case BRICK -> "CARNIE_ARMOR4";
            case DARK_ARMOR -> "DARK_ARMOR3";
            case GARGANTUAR -> "EGYPT_GARGANTUAR";
            case IMP -> "DARK_IMP";
            case ALL_STAR -> "MODERN_ALLSTAR";
            case ARCADE -> "EIGHTIES_ARCADE";
            case JANE -> "LOSTCITY_JANE";
            case CRYSTAL_SKULL -> "LOSTCITY_CRYSTALSKULL";
            case PROSPECTOR -> "PROSPECTOR";
            case PIANIST -> "PIANO";
            case NEWSPAPER -> "MODERN_NEWSPAPER";
            case BARREL_ROLLER -> "BARRELROLLER";

            case RA -> "RA";
            case EXPLORER -> "EXPLORER_VETERAN";
            case TOMB_RAISER -> "TOMB_RAISER";

            case DODO -> "ICEAGE_DODO";
            case HUNTER -> "ICEAGE_HUNTER";
            case TROGLOBITE -> "ICEAGE_TROGLOBITE";

            case FISHERMAN -> "BEACH_FISHERMAN";
            case OCTOPUS -> "BEACH_OCTOPUS";
            case SNORKEL -> "BEACH_SNORKEL";

            case JUGGLER -> "DARK_JUGGLER";
            case WIZARD -> "DARK_WIZARD";
            case KING -> "DARK_KING";
            case IMP_DRAGON -> "DARK_IMP_DRAGON";

            case ZOMBOTANY_PEASHOOTER, ZOMBOTANY_WALLNUT, ZOMBOTANY_JALAPENO, ZOMBOTANY_SQUASH -> "TUTORIAL";
            default -> "TUTORIAL";
        };
    }

    public static Stack imageHoverStack(TextureBank textures, String imageId, float width, float height,
                                        float hoverScale, float clickScale,
                                        ButtonAnimator.OnClickListener clickListener) {
        Stack stack = new Stack();
        stack.setTransform(true);
        stack.setSize(width, height);

        Image image = imageFor(textures, imageId);
        image.setScaling(Scaling.fit);
        stack.add(image);

        if (clickListener != null) {
            stack.setTouchable(Touchable.enabled);
            ButtonAnimator.applyHoverAndClickEffect(stack, hoverScale, clickScale, clickListener);
        }
        return stack;
    }

    public static Stack screenTitle(String text, Skin skin, float scale) {
        Stack stack = new Stack();

        Label shadow = new Label(text, skin, "big");
        shadow.setFontScale(scale);
        shadow.setColor(Color.valueOf("#2A1A0C"));
        shadow.setAlignment(Align.center);
        Container<Label> shadowContainer = new Container<>(shadow);
        shadowContainer.padTop(5).padLeft(5);

        Label front = new Label(text, skin, "big");
        front.setFontScale(scale);
        front.setColor(Color.valueOf("#FFD86B"));
        front.setAlignment(Align.center);

        stack.add(shadowContainer);
        stack.add(front);
        return stack;
    }
}
