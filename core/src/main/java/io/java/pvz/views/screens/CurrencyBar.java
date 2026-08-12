package io.java.pvz.views.screens;


import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.models.App;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

public class CurrencyBar extends Table {
    private static final int COIN_CHEAT_AMOUNT = 100;
    private static final int DIAMOND_CHEAT_AMOUNT = 10;

    private final Label coinLabel;
    private final Label diamondLabel;

    public CurrencyBar(TextureBank texture, Skin skin) {
        Stack diamondDisplay = createDisplay(texture, skin, Ids.GameScreen.GEM_ICON, this::cheatDiamonds);
        this.diamondLabel = (Label) diamondDisplay.getUserObject();

        Stack coinDisplay = createDisplay(texture, skin, Ids.GameScreen.COIN_ICON, this::cheatCoin);
        this.coinLabel = (Label) coinDisplay.getUserObject();

        add(diamondDisplay).width(160).height(60).padRight(40);
        add(coinDisplay).width(160).height(60).padRight(40);

        refresh();
    }


    private Stack createDisplay(TextureBank texture, Skin skin, String imageId, Runnable onLabelClick) {
        Stack displayStack = new Stack();

        Image bgImage = UiFactory.imageFor(texture, imageId);
        bgImage.setScaling(Scaling.fit);
        Container<Image> bgContainer = new Container(bgImage);
        bgContainer.size(160, 60);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        Label value = new Label("0", labelStyle);
        value.setAlignment(Align.left | Align.center);
        value.setTouchable(Touchable.enabled);
        displayStack.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               onLabelClick.run();
           }
        });

        Container<Label> labelContainer = new Container<>(value);
        labelContainer.padLeft(45);

        displayStack.add(bgContainer);
        displayStack.add(labelContainer);
        displayStack.setUserObject(value);

        return displayStack;
    }

    private void cheatCoin() {
        User user = App.getActiveUser();
        if (user == null) return;
        new GameMenuController().cheat(DIAMOND_CHEAT_AMOUNT,"coin");
        refresh();
    }

    private void cheatDiamonds() {
        User user = App.getActiveUser();
        if (user == null) return;
        new GameMenuController().cheat(DIAMOND_CHEAT_AMOUNT,"diamond");
        refresh();
    }

    public void refresh() {
        User user = App.getActiveUser();
        if (user == null) return;
        coinLabel.setText(String.valueOf(user.getCoin()));
        diamondLabel.setText(String.valueOf(user.getDiamond()));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refresh();
    }
}
