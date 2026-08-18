package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.MatchmakingController;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.modals.MatchFoundTable;
import io.java.pvz.views.screens.modals.WaitingTable;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class MultiplayerLobbyScreen extends BaseScreen {

    private final Skin skin;
    private final TextureBank textures;
    private TextureRegion backgroundRegion;

    private final MatchmakingController matchmaking = MatchmakingController.getInstance();

    private String outgoingInviteId;
    private boolean waitingInQueue;
    private WaitingTable activeWaitingModal;

    public MultiplayerLobbyScreen(Game game) {
        super(game);
        skin = AssetLoader.getInstance().getSkin();
        textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        registerLobbyHandlers();
        showIdleState();
    }

    private void registerLobbyHandlers() {
        matchmaking.setOnChallengeDeclined(reason -> {
            outgoingInviteId = null;
            closeWaitingModal();
            notify("Your Request Denied: " + reason);
        });

        matchmaking.setOnMatchFound(info -> {
            outgoingInviteId = null;
            waitingInQueue = false;
            closeWaitingModal();
            new MatchFoundTable(skin, info.opponentUsername, info.role).show(modalLayer, viewport);
        });
    }

    private void closeWaitingModal() {
        if (activeWaitingModal != null) {
            activeWaitingModal.remove();
            activeWaitingModal = null;
        }
    }

    private void showIdleState() {
        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topBar = new Table();
        topBar.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            this::onBackPressed)).left();
        topBar.add().expandX();
        mainLayer.add(topBar).growX().padTop(20).padLeft(30).row();

        mainLayer.add(UiFactory.screenTitle("IZombie", skin, 1.5f))
            .padTop(10).padBottom(50).row();

        BorderedTable card = new BorderedTable();
        card.pad(50);
        card.defaults().pad(12).width(520);

        if (!NetworkController.getInstance().isAuthenticated()) {
            Label warn = new Label(
                "You are currently offline.\nPlay Single Player or Login for Multiplayer.",
                skin);
            warn.setColor(Color.valueOf("#4A3018"));
            warn.setFontScale(1.15f);
            warn.setAlignment(Align.center);
            warn.setWrap(true);
            card.add(warn).height(100).row();

            TextButton goLoginBtn = UiFactory.textButton("Return to Login Page", skin, "purple", 1.05f, 0.95f,
                () -> ScreenManager.getInstance().setRootScreen(new LoginScreen(game)));
            goLoginBtn.getLabel().setFontScale(1.1f);
            card.add(goLoginBtn).height(70).row();

            mainLayer.add(card).expand().center();
            return;
        }

        TextButton specificBtn = UiFactory.textButton("Play With specific Player", skin, "purple", 1.05f, 0.95f,
            this::showSpecificOpponentState);
        specificBtn.getLabel().setFontScale(1.2f);
        card.add(specificBtn).height(90).row();

        TextButton randomBtn = UiFactory.textButton("Play Random", skin, "green_small", 1.05f, 0.95f,
            this::joinRandomMatch);
        randomBtn.getLabel().setFontScale(1.2f);
        card.add(randomBtn).height(90).row();

        mainLayer.add(card).expand().center();
    }

    private void onBackPressed() {
        if (waitingInQueue) {
            matchmaking.leaveRandomQueue(response -> {
            });
            waitingInQueue = false;
        }
        closeWaitingModal();
        ScreenManager.getInstance().popScreen();
    }

    private void showSpecificOpponentState() {
        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topBar = new Table();
        topBar.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            this::showIdleState)).left();
        topBar.add().expandX();
        mainLayer.add(topBar).growX().padTop(20).padLeft(30).row();

        mainLayer.add(UiFactory.screenTitle("Play With specific Player", skin, 1.4f)).padTop(10).padBottom(50).row();

        BorderedTable card = new BorderedTable();
        card.pad(50);
        card.defaults().pad(12);

        Label hint = new Label("Please Enter the Username of your opponent:", skin);
        hint.setColor(Color.valueOf("#4A3018"));
        hint.setFontScale(1.15f);
        hint.setAlignment(Align.center);
        card.add(hint).width(520).row();

        TextField.TextFieldStyle fieldStyle = buildFieldStyle();
        TextField usernameField = new TextField("", fieldStyle);
        usernameField.setMessageText("Username");
        usernameField.setAlignment(Align.center);
        card.add(usernameField).height(60).width(420).row();

        TextButton sendBtn = UiFactory.textButton("Send Request", skin, "purple", 1.05f, 0.95f, () ->
            sendChallengeTo(usernameField.getText()));
        sendBtn.getLabel().setFontScale(1.15f);
        card.add(sendBtn).height(70).width(300).row();

        mainLayer.add(card).expand().center();
    }

    private void sendChallengeTo(String rawUsername) {
        String username = rawUsername == null ? "" : rawUsername.trim();
        if (username.isEmpty()) {
            notify("Please Enter the username of your opponent");
            return;
        }

        matchmaking.sendChallenge(username, response -> {
            if (response == null || !response.isSuccess()) {
                assert response != null;
                notify(response.getErrorMessage());
                return;
            }
            outgoingInviteId = response.getString("inviteId");
            showWaitingForResponse(username);
        });
    }

    private void showWaitingForResponse(String opponentUsername) {
        closeWaitingModal();
        activeWaitingModal = new WaitingTable(skin, "Waiting For Response " + opponentUsername);
        activeWaitingModal.setOnCancel(() -> {
            outgoingInviteId = null;
        });
        activeWaitingModal.show(modalLayer, viewport);
    }

    private void joinRandomMatch() {
        matchmaking.joinRandomQueue(response -> {
            if (response == null || !response.isSuccess()) {
                assert response != null;
                notify(response.getErrorMessage());
                return;
            }

            String status = response.getString("status");
            if ("waiting".equals(status)) {
                waitingInQueue = true;
                showWaitingInQueue();
            }
        });
    }

    private void showWaitingInQueue() {
        closeWaitingModal();
        activeWaitingModal = new WaitingTable(skin, "In Queue ...");
        activeWaitingModal.setOnCancel(() -> {
            waitingInQueue = false;
            matchmaking.leaveRandomQueue(response -> {
            });
        });
        activeWaitingModal.show(modalLayer, viewport);
    }

    private TextField.TextFieldStyle buildFieldStyle() {
        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);
        customFieldStyle.font = skin.getFont("FBUSV8C5EI_1");
        customFieldStyle.messageFont = skin.getFont("FBUSV8C5EI_1");
        Label.LabelStyle labelStyle = skin.get("bundle_reward_multiplier", Label.LabelStyle.class);
        Drawable woodBackground = labelStyle.background;
        customFieldStyle.background = woodBackground;
        customFieldStyle.focusedBackground = woodBackground;
        return customFieldStyle;
    }

    @Override
    public void render(float delta) {
        clearScreen(0.02f, 0.15f, 0.16f, 1f);

        AssetLoader.getInstance().updateTextures();

        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }
}
