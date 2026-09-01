package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.ProfileNetworkController;
import io.java.pvz.controllers.MenuController.ProfileMenuController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import org.jspecify.annotations.NonNull;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class ProfileModalTable extends BorderedTable {

    private final ProfileNetworkController networkController;
    private final Skin skin;
    private final TextureBank textures;

    private Actor blocker;

    private final Color brownColor = Color.valueOf("#4A3018");
    private final Color lightBrown = Color.valueOf("#8B5A2B");

    private TextField nicknameField;
    private TextField emailField;
    private TextField usernameField;

    public ProfileModalTable(Skin skin) {
        super();
        this.skin = skin;
        this.textures = AssetLoader.getInstance().getTextures();
        this.networkController = new ProfileNetworkController();

        pad(40, 45, 35, 45);
        setSize(800, 950);

        buildContent();
    }

    private void buildContent() {
        User user = App.getActiveUser();
        if (user == null) {
            this.remove();
            return;
        }

        Table headerTable = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);

        Label titleLabel = new Label("User Profile", skin, "big");
        titleLabel.setColor(brownColor);
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(45, 45).left();
        headerTable.add(titleLabel).expandX().center().padRight(45);

        add(headerTable).growX().padBottom(30).row();

        Table scrollContent = new Table();
        scrollContent.top();

        buildInfoSection(scrollContent, user);
        buildEditSection(scrollContent, user);
        buildPasswordSection(scrollContent);

        ScrollPane scrollPane = new ScrollPane(scrollContent, skin) {
            @Override
            protected void setStage(Stage stage) {
                super.setStage(stage);
                if (stage != null) stage.setScrollFocus(this);
            }
        };
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(scrollPane).grow().row();
    }

    private TextField.TextFieldStyle buildWoodFieldStyle() {
        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);

        BitmapFont font = skin.getFont("FBUSV8C5EI_1");
        font.getData().setScale(0.9f);

        customFieldStyle.font = font;
        customFieldStyle.messageFont = font;
        customFieldStyle.fontColor = brownColor;
        customFieldStyle.messageFontColor = lightBrown;

        Drawable woodBackground = skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background;
        customFieldStyle.background = woodBackground;
        customFieldStyle.focusedBackground = woodBackground;

        return customFieldStyle;
    }

    private void buildInfoSection(Table parent, User user) {
        Table infoTable = new Table();
        infoTable.left().defaults().left().padBottom(15);

        Table nameHolder = new Table();
        Image profileIcon = UiFactory.imageFor(textures, Ids.MainMenu.PROFILE_ICON);
        profileIcon.setScaling(Scaling.fit);
        Label userLabel = new Label(user.getUsername(), skin, "big");
        userLabel.setColor(Color.ORANGE);
        userLabel.setFontScale(1.4f);

        nameHolder.add(profileIcon).size(60).padRight(15);
        nameHolder.add(userLabel);

        infoTable.add(nameHolder).colspan(4).padBottom(25).row();

        infoTable.add(createStatLabel("Games Played: ")).padRight(10);
        infoTable.add(createStatValue(user.getGamesPlayed())).padRight(100);

        infoTable.add(createStatLabel("Levels Completed: ")).padRight(10);
        infoTable.add(createStatValue(user.getLevelsCompleted())).row();

        infoTable.add(createStatLabel("Coins: ")).padRight(10);
        infoTable.add(createStatValue(user.getCoin())).padRight(100);

        infoTable.add(createStatLabel("Diamonds: ")).padRight(10);
        infoTable.add(createStatValue(user.getDiamond())).row();

        parent.add(infoTable).growX().pad(10, 20, 30, 20).row();

    }

    private void buildEditSection(Table parent, User user) {
        Table editTable = new Table();
        editTable.left().defaults().left().padBottom(15);

        Label sectionTitle = new Label("Edit Personal Info", skin);
        sectionTitle.setColor(brownColor);
        sectionTitle.setFontScale(2.2f);
        editTable.add(sectionTitle).colspan(2).padBottom(20).row();

        TextField.TextFieldStyle fieldStyle = buildWoodFieldStyle();

        editTable.add(createFieldLabel("Nickname:")).width(150);
        nicknameField = new TextField(user.getNickname(), fieldStyle);
        nicknameField.setMessageText("new nickname");
        nicknameField.setAlignment(Align.center);
        editTable.add(nicknameField).width(400).height(60).row();

        editTable.add(createFieldLabel("Email:")).width(150);
        emailField = new TextField(user.getEmail(), fieldStyle);
        emailField.setMessageText("new email");
        emailField.setAlignment(Align.center);
        editTable.add(emailField).width(400).height(60).row();

        editTable.add(createFieldLabel("Username:")).width(150);
        usernameField = new TextField(user.getUsername(), fieldStyle);
        usernameField.setMessageText("new username");
        usernameField.setAlignment(Align.center);
        editTable.add(usernameField).width(400).height(60).row();

        TextButton saveInfoBtn = UiFactory.textButton("Save Info", skin, "green_small", 1.1f,
            0.9f, () ->
            saveProfileInfo(nicknameField.getText(), emailField.getText(), usernameField.getText()));
        saveInfoBtn.getLabel().setFontScale(1.2f);
        editTable.add();
        editTable.add(saveInfoBtn).width(200).height(60).padTop(10).center().row();

        parent.add(editTable).growX().pad(10, 20, 40, 20).row();
    }

    private void saveProfileInfo(String newNickname, String newEmail, String newUsername) {
        if (!NetworkController.getInstance().isAuthenticated()) {
            dispatchMessage("Error: You must be online to change profile info.");
            return;
        }

        User currentUser = App.getActiveUser();
        if (currentUser == null) return;

        boolean isChanged = false;

        if (!newNickname.isEmpty() && !newNickname.equals(currentUser.getNickname())) {
            networkController.updateNickname(newNickname, response -> handleNetworkResponse("Nickname", response));
            isChanged = true;
        }

        if (!newEmail.isEmpty() && !newEmail.equals(currentUser.getEmail())) {
            networkController.updateEmail(newEmail, response -> handleNetworkResponse("Email", response));
            isChanged = true;
        }

        if (!newUsername.isEmpty() && !newUsername.equals(currentUser.getUsername())) {
            networkController.updateUsername(newUsername, response -> handleNetworkResponse("Username", response));
            isChanged = true;
        }
        if (!isChanged)
            dispatchMessage("No changes were made to your profile");
    }

    private void buildPasswordSection(Table parent) {
        Table passTable = new Table();
        passTable.left().defaults().left().padBottom(15);

        Label sectionTitle = new Label("Change Password", skin);
        sectionTitle.setColor(brownColor);
        sectionTitle.setFontScale(2.2f);
        passTable.add(sectionTitle).colspan(2).padBottom(20).row();

        TextField.TextFieldStyle fieldStyle = buildWoodFieldStyle();

        passTable.add(createFieldLabel("Old Password:")).width(150);
        TextField oldPassField = new TextField("", fieldStyle);
        oldPassField.setPasswordMode(true);
        oldPassField.setPasswordCharacter('*');
        oldPassField.setMessageText("current password");
        oldPassField.setAlignment(Align.center);
        passTable.add(oldPassField).width(400).height(60).row();

        passTable.add(createFieldLabel("New Password:")).width(150);
        TextField newPassField = new TextField("", fieldStyle);
        newPassField.setPasswordMode(true);
        newPassField.setPasswordCharacter('*');
        newPassField.setMessageText("new password");
        newPassField.setAlignment(Align.center);
        passTable.add(newPassField).width(400).height(60).row();

        passTable.add(createFieldLabel("Repeat New:")).width(150);
        TextField repeatPassField = new TextField("", fieldStyle);
        repeatPassField.setPasswordMode(true);
        repeatPassField.setPasswordCharacter('*');
        repeatPassField.setMessageText("repeat new password");
        repeatPassField.setAlignment(Align.center);
        passTable.add(repeatPassField).width(400).height(60).row();

        TextButton changePassBtn = generatePassChange(oldPassField, newPassField, repeatPassField);
        passTable.add();
        passTable.add(changePassBtn).width(200).height(60).padTop(10).center().row();

        parent.add(passTable).growX().pad(10, 20, 40, 20).row();

    }

    private @NonNull TextButton generatePassChange(TextField oldPassField,
                                                   TextField newPassField,
                                                   TextField repeatPassField) {
        return UiFactory.textButton("Change Password", skin, "purple", 1.05f, 0.95f, () -> {
            String oldPassword = oldPassField.getText();
            String newPassword = newPassField.getText();
            String repeatPassword = repeatPassField.getText();

            if (!NetworkController.getInstance().isAuthenticated()) {
                dispatchMessage("Error: You must be online to change password.");
                return;
            }

            networkController.updatePassword(oldPassword, newPassword, repeatPassword, response -> {
                Gdx.app.postRunnable(() -> {
                    handleNetworkResponse("Password", response);
                    if (response != null && response.isSuccess()) {
                        oldPassField.setText("");
                        newPassField.setText("");
                        repeatPassField.setText("");
                    }
                });
            });
        });
    }

    private void handleNetworkResponse(String fieldName, NetworkMessage response) {
        Gdx.app.postRunnable(() -> {
            if (response != null && response.isSuccess()) {
                dispatchMessage(fieldName + " updated successfully!");

                User current = App.getActiveUser();
                if (current != null) {
                    if (fieldName.equals("Username") && response.getString("username") != null) {
                        current.setUsername(response.getString("username"));
                    } else if (fieldName.equals("Email") && response.getString("email") != null) {
                        current.setEmail(response.getString("email"));
                    } else if (fieldName.equals("Nickname") && response.getString("nickname") != null) {
                        current.setNickname(response.getString("nickname"));
                    }
                }
            } else {
                String error = response != null ? response.getErrorMessage() : "server unreachable";
                dispatchMessage("Failed to update " + fieldName + ": " + error);
            }
        });
    }

    private void dispatchMessage(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY).message(message).build());
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(brownColor);
        label.setFontScale(2f);
        return label;
    }

    private Label createStatValue(Object value) {
        Label label = new Label(String.valueOf(value), skin);
        label.setColor(lightBrown);
        label.setFontScale(1.5f);
        return label;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(brownColor);
        label.setFontScale(1.6f);
        return label;
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
