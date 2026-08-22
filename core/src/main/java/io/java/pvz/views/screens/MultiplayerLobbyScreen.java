package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.controllers.GameController.MatchController;
import io.java.pvz.controllers.GameController.MatchmakingController;
import io.java.pvz.controllers.GameController.TravelLogController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.MiniGameFactory;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.models.users.User;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.client.ServerConfig;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.PlayerRole;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.gameflow.GameFlowScreen;
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
        checkServerAndShowUI();
    }

    private void checkServerAndShowUI() {
        mainLayer.clear();
        mainLayer.setFillParent(true);

        Label checkingLabel = new Label("Connecting to Server...", skin, "big");
        checkingLabel.setColor(Color.valueOf("#4A3018"));
        mainLayer.add(checkingLabel).center();

        new Thread(() -> {
            boolean online = false;
            try {
                if (!NetworkClient.getInstance().isConnected()) {
                    NetworkClient.getInstance().connect(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT);

                    User user = App.getActiveUser();
                    if (user != null) {
                        NetworkMessage loginReq = NetworkMessage.request(MessageType.LOGIN);
                        loginReq.put("username", user.getUsername());
                        loginReq.put("password", user.getPassword());
                        loginReq.put("stayLoggedIn", true);
                        loginReq.put("isHash", true);

                        NetworkMessage res = NetworkClient.getInstance().sendAndWait(loginReq, 5);
                        online = res.isSuccess();
                    }
                } else {
                    online = true;
                }
            } catch (Exception e) {
                online = false;
            }

            final boolean finalOnline = online;
            com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                showIdleState(finalOnline);
            });
        }, "ServerPingThread").start();
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

            MatchController.getInstance().setupOnlineMatch(
                PlayerRole.valueOf(info.role)
            );

            Level izombie = MiniGameFactory.createLevel(MiniGameType.I_ZOMBIE, 2);
            GameSession.startMiniGame(izombie, App.getActiveUser().getUnlockedPlants());
            new MatchFoundTable(skin, info.opponentUsername, info.role).show(modalLayer, viewport);

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    String mapId = new GameMenuController().getCurrentMapTextureId();
                    ScreenManager.getInstance().pushScreen(new GameFlowScreen(game, mapId));
                }
            }, 2.0f);
        });
    }

    private void closeWaitingModal() {
        if (activeWaitingModal != null) {
            activeWaitingModal.remove();
            activeWaitingModal = null;
        }
    }

    private void showIdleState(boolean isServerOnline) {
        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topBar = new Table();
        topBar.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            this::onBackPressed)).left();
        topBar.add().expandX();
        mainLayer.add(topBar).growX().padTop(20).padLeft(30).row();

        mainLayer.add(UiFactory.screenTitle("I, Zombie - Select Mode", skin, 1.5f))
            .padTop(10).padBottom(40).row();

        BorderedTable card = new BorderedTable();
        card.pad(40);
        card.defaults().pad(10).width(520);

        TextButton singleBtn = UiFactory.textButton("1-Player (vs Bot)", skin, "green_small", 1.05f, 0.95f,
            this::startSinglePlayer);
        singleBtn.getLabel().setFontScale(1.2f);
        card.add(singleBtn).height(80).row();

        TextButton couchBtn = UiFactory.textButton("2-Player (Couch Play)", skin, "green_small", 1.05f, 0.95f,
            this::startCouchPlay);
        couchBtn.getLabel().setFontScale(1.2f);
        card.add(couchBtn).height(80).row();

        if (isServerOnline) {
            TextButton specificBtn = UiFactory.textButton("Play With Friend (Online)", skin, "purple", 1.05f, 0.95f,
                this::showSpecificOpponentState);
            specificBtn.getLabel().setFontScale(1.2f);
            card.add(specificBtn).height(80).padTop(15).row();

            TextButton randomBtn = UiFactory.textButton("Play Random (Online)", skin, "purple", 1.05f, 0.95f,
                this::joinRandomMatch);
            randomBtn.getLabel().setFontScale(1.2f);
            card.add(randomBtn).height(80).row();
        } else {
            Label warn = new Label("(Game Server is Offline - Local Play Only)", skin);
            warn.setColor(Color.valueOf("#4A3018"));
            warn.setFontScale(1.1f);
            warn.setAlignment(Align.center);
            card.add(warn).padTop(15).row();
        }

        mainLayer.add(card).expand().center();
    }

    private void startSinglePlayer() {
        MatchController.getInstance().setOnlineMatch(false);
        MatchController.getInstance().setCouchPlay(false);

        TravelLogController travelLogController = new TravelLogController();
        travelLogController.changePage("minigame");

        ScreenManager.getInstance().pushScreen(
            new LevelSelectionScreen(game, MiniGameType.I_ZOMBIE, travelLogController)
        );
    }

    private void startCouchPlay() {
        MatchController.getInstance().setOnlineMatch(false);
        MatchController.getInstance().setCouchPlay(true);

        Level izombie = MiniGameFactory.createLevel(MiniGameType.I_ZOMBIE, 2);
        GameSession.startMiniGame(izombie, null);
        String mapId = new GameMenuController().getCurrentMapTextureId();
        ScreenManager.getInstance().pushScreen(new GameFlowScreen(game, mapId));
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
            this::checkServerAndShowUI)).left();
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
                notify(response != null ? response.getErrorMessage() : "Error sending challenge");
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
                notify(response != null ? response.getErrorMessage() : "Error joining queue");
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
