package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.MenuController.LoginMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class LoginScreen extends BaseScreen {

    private final LoginMenuController loginController;
    private TextureRegion backgroundRegion;
    private final Skin skin;
    private final TextureBank textures;

    public LoginScreen(Game game) {
        super(game);
        skin = AssetLoader.getInstance().getSkin();
        this.loginController = new LoginMenuController();
        textures = AssetLoader.getInstance().getTextures();

        buildUI();
    }

    private void buildUI() {
        TextField.TextFieldStyle customFieldStyle = buildStyle();

        Table baseTable = buildBaseTable();

        TextField usernameField = new TextField("", customFieldStyle);
        usernameField.setMessageText("Username");
        usernameField.setAlignment(Align.center);
        baseTable.add(usernameField).height(60).row();

        TextField passwordField = new TextField("", customFieldStyle);
        passwordField.setMessageText("Password");
        passwordField.setAlignment(Align.center);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        baseTable.add(passwordField).height(60).row();

        buildButtons(baseTable, usernameField, passwordField);

        mainLayer.add(baseTable).expand().center();
    }

    private Table buildBaseTable() {
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        mainLayer.clear();
        mainLayer.setFillParent(true);

        BorderedTable centerTable = new BorderedTable();
        centerTable.pad(70);
        centerTable.defaults().pad(5).width(400);

        return centerTable;
    }

    private TextField.TextFieldStyle buildStyle() {
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

    private void buildButtons(Table baseTable, TextField usernameField, TextField passwordField) {

        CheckBox stayLoggedIn = new CheckBox(" Stay logged in", skin);
        stayLoggedIn.getLabel().setFontScale(2);
        stayLoggedIn.getLabel().setColor(Color.BROWN);
        stayLoggedIn.setScale(2);
        baseTable.add(stayLoggedIn).left().padTop(15).padBottom(15).row();

        TextButton loginBtn = UiFactory.textButton("Login", skin, "purple", 1.05f, 0.95f, () -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            Result result = loginController.login(user, pass, stayLoggedIn.isChecked());
            if (result.isSuccessful()) {
                System.out.println("Login Success: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Login Success: " + result.message())
                        .build());
                authenticateWithServer(user, pass);
                ScreenManager.getInstance().setRootScreen(new MainMenuScreen(game));
            } else {
                System.out.println("Login Failed: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Login Failed: " + result.message())
                        .build());
            }
        });
        loginBtn.getLabel().setFontScale(1.2f);

        baseTable.add(loginBtn).width(300).height(70).padTop(10).row();

        TextButton forgotBtn = UiFactory.textButton("Forgot Password?", skin, "green_small",
            1.05f, 0.95f, this::showForgotPasswordStep1);
        baseTable.add(forgotBtn).width(300).height(50).padTop(10).row();

        TextButton gotoSignupBtn = UiFactory.textButton("Don't have an account? Sign up", skin, "green_small",
            1.05f, 0.95f, () -> {
                ScreenManager.getInstance().pushScreen(new SignupScreen(game));
            });
        baseTable.add(gotoSignupBtn).width(300).height(50).padTop(5).row();
    }

    private void authenticateWithServer(String username, String password) {
        NetworkController.getInstance().login(username, password, response -> {
            if (response != null && response.isSuccess()) {
                System.out.println("Connected to game server as " + username + " (online features enabled)");
            } else {
                String reason = response != null ? response.getErrorMessage() : "server unreachable";
                System.out.println("Online features unavailable: " + reason);
            }
        });
    }

    @Override
    public void render(float delta) {
        clearScreen(0.05f, 0.05f, 0.1f, 1f);

        AssetLoader.getInstance().updateTextures();

        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }

    private void showForgotPasswordStep1() {
        mainLayer.clear();
        modalLayer.clearChildren();
        TextField.TextFieldStyle style = buildStyle();

        BorderedTable popup = new BorderedTable();

        popup.pad(40);
        popup.defaults().pad(10);

        TextButton backBtn = UiFactory.textButton("Back", skin, "green_small", 1.05f, 0.95f, () -> {
            modalLayer.clearChildren();
            buildUI();
        });
        popup.add(backBtn).width(100).height(50).left().padBottom(15).row();

        Label title = new Label("Forgot Password", skin);
        title.setColor(Color.BROWN);
        title.setFontScale(1.5f);
        popup.add(title).padBottom(20).row();

        TextField userField = new TextField("", style);
        userField.setMessageText("Username");
        userField.setAlignment(Align.center);

        TextField emailField = new TextField("", style);
        emailField.setMessageText("Email Address");
        emailField.setAlignment(Align.center);

        TextButton nextBtn = UiFactory.textButton("Next", skin, "green_small", 1.05f, 0.95f, () -> {
            Result result = loginController.forgetPassword(userField.getText(), emailField.getText());
            if (result.isSuccessful()) {
                showForgotPasswordStep2(result.message());
            } else {
                System.out.println("Error: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Error: " + result.message())
                        .build());
                modalLayer.clearChildren();
                buildUI();
            }
        });
        nextBtn.getLabel().setFontScale(1.2f);

        popup.add(userField).height(65).width(350).padBottom(10).row();
        popup.add(emailField).height(65).width(350).padBottom(20).row();
        popup.add(nextBtn).width(180).height(60).row();

        modalLayer.add(popup).center();
    }

    private void showForgotPasswordStep2(String securityQuestion) {
        modalLayer.clearChildren();
        TextField.TextFieldStyle style = buildStyle();

        BorderedTable popup = new BorderedTable();

        popup.pad(40);
        popup.defaults().pad(10);

        TextButton backBtn = UiFactory.textButton("Back", skin, "green_small",
            1.05f, 0.95f, this::showForgotPasswordStep1);
        popup.add(backBtn).width(100).height(50).left().padBottom(15).row();

        Label title1 = new Label("Security Question:", skin);
        title1.setColor(Color.BROWN);
        title1.setFontScale(1.5f);
        popup.add(title1).padBottom(5).row();

        Label title2 = new Label(securityQuestion, skin);
        title2.setColor(Color.BROWN);
        title2.setFontScale(1.5f);
        popup.add(title2).padBottom(20).row();

        TextField answerField = new TextField("", style);
        answerField.setMessageText("Your Answer");
        answerField.setAlignment(Align.center);

        TextButton verifyBtn = UiFactory.textButton("Verify", skin, "green_small", 1.05f, 0.95f, () -> {
            Result result = loginController.checkSecurityQuestion(answerField.getText());
            if (result.isSuccessful()) {
                showForgotPasswordStep3();
            } else {
                System.out.println("Wrong Answer: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Wrong Answer: " + result.message())
                        .build());
                modalLayer.clearChildren();
                buildUI();
            }
        });
        verifyBtn.getLabel().setFontScale(1.2f);

        popup.add(answerField).height(65).width(350).padBottom(20).row();
        popup.add(verifyBtn).width(180).height(60).row();

        modalLayer.add(popup).center();
    }

    private void showForgotPasswordStep3() {
        modalLayer.clearChildren();
        TextField.TextFieldStyle style = buildStyle();

        BorderedTable popup = new BorderedTable();

        popup.pad(40);
        popup.defaults().pad(10);

        TextButton backBtn = UiFactory.textButton("Back", skin, "green_small",
            1.05f, 0.95f, this::showForgotPasswordStep1);
        popup.add(backBtn).width(100).height(50).left().padBottom(15).row();

        TextField passField = new TextField("", style);
        passField.setMessageText("New Password");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');
        passField.setAlignment(Align.center);

        TextField repeatPassField = new TextField("", style);
        repeatPassField.setMessageText("Repeat Password");
        repeatPassField.setPasswordMode(true);
        repeatPassField.setPasswordCharacter('*');
        repeatPassField.setAlignment(Align.center);

        TextButton changeBtn = UiFactory.textButton("Change Password", skin, "purple", 1.05f, 0.95f, () -> {
            Result result = loginController.resetPassword(passField.getText(), repeatPassField.getText());
            if (result.isSuccessful()) {
                modalLayer.clearChildren();
                buildUI();
                System.out.println("Password changed successfully!");
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Password changed successfully!")
                        .build());
            } else {
                System.out.println("error: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Error: " + result.message())
                        .build());
                modalLayer.clearChildren();
                buildUI();
            }
        });
        changeBtn.getLabel().setFontScale(1.2f);

        popup.add(passField).height(65).width(350).padBottom(10).row();
        popup.add(repeatPassField).height(65).width(350).padBottom(20).row();
        popup.add(changeBtn).height(70).width(250).row();

        modalLayer.add(popup).center();
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }
}
