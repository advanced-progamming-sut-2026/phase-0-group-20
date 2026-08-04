package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.MenuController.LoginMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.utils.Ids;
import pvz.libpvz.textures.TextureBank;

public class LoginScreen extends BaseScreen {

    private final LoginMenuController loginController;
    private TextureRegion backgroundRegion;

    public LoginScreen(Game game) {
        super(game);
        this.loginController = new LoginMenuController();
        buildUI();
    }

    private void buildUI() {
        Skin skin = AssetLoader.getInstance().getSkin();
        TextField.TextFieldStyle customFieldStyle = buildStyle(skin);

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

        TextButton loginBtn = new TextButton("Login", skin, "purple");
        loginBtn.getLabel().setFontScale(1.2f);
        ButtonAnimator.applyHoverAndClickEffect(loginBtn, 1.1f, 0.9f, () -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();

            Result result = loginController.login(user, pass, true);
            ScreenManager.getInstance().setRootScreen(new MainMenuScreen(game));//for now
//            if (result.isSuccessful()) {
//                System.out.println("Login Success: " + result.message());
//
//            } else {
//                System.out.println("Login Failed: " + result.message());
//                // TODO: show error on toastLayer
//            }
        });
        baseTable.add(loginBtn).height(80).padTop(20).row();

        TextButton gotoSignupBtn = new TextButton("Don't have an account? Sign up", skin);
        ButtonAnimator.applyHoverAndClickEffect(gotoSignupBtn, 1.05f, 0.95f, () -> {
            ScreenManager.getInstance().pushScreen(new SignupScreen(game));
        });
        baseTable.add(gotoSignupBtn).height(60).padTop(10).row();

        mainLayer.add(baseTable).expand().center();
    }

    private Table buildBaseTable() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        mainLayer.clear();
        mainLayer.setFillParent(true);

        Table centerTable = new Table();
        centerTable.defaults().pad(5).width(400);

        return centerTable;
    }

    private TextField.TextFieldStyle buildStyle(Skin skin) {

        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);

        customFieldStyle.background = null;
        customFieldStyle.focusedBackground = null;
        customFieldStyle.font = skin.getFont("FBUSV8C5EI_1");

        return customFieldStyle;
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
}
