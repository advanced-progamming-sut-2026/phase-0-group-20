package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.MenuController.SignupMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.utils.Ids;
import pvz.libpvz.textures.TextureBank;

public class SignupScreen extends BaseScreen {

    private final SignupMenuController signupController;
    private TextureRegion backgroundRegion;

    public SignupScreen(Game game) {
        super(game);
        this.signupController = new SignupMenuController();
        buildUI();
    }

    private void buildUI() {
        Skin skin = AssetLoader.getInstance().getSkin();

        Table baseTable = buildBaseTable();
        TextField.TextFieldStyle customFieldStyle = buildStyle(skin);

        TextField usernameField = createField("Username", false, customFieldStyle);
        TextField passwordField = createField("Password", true, customFieldStyle);
        TextField repeatPasswordField = createField("Repeat Password", true, customFieldStyle);
        TextField nicknameField = createField("Nickname", false, customFieldStyle);
        TextField emailField = createField("Email", false, customFieldStyle);
        TextField genderField = createField("Gender (MALE/FEMALE)", false, customFieldStyle);

        baseTable.add(usernameField).height(50).row();
        baseTable.add(passwordField).height(50).row();
        baseTable.add(repeatPasswordField).height(50).row();
        baseTable.add(nicknameField).height(50).row();
        baseTable.add(emailField).height(50).row();
        baseTable.add(genderField).height(50).row();

        TextButton registerBtn = new TextButton("Register", skin, "purple");
        ButtonAnimator.applyHoverAndClickEffect(registerBtn, 1.1f, 0.9f, () -> {
            Result result = signupController.register(usernameField.getText(), passwordField.getText(),
                repeatPasswordField.getText(), nicknameField.getText(), emailField.getText(), genderField.getText()
            );

            if (result.isSuccessful()) {
                System.out.println("Registration Initial Step Success!");
                // TODO: Security questions
            } else System.out.println("Registration Failed: " + result.message());

        });
        baseTable.add(registerBtn).height(70).padTop(15).row();

        TextButton backBtn = new TextButton("Already have account? Login", skin);
        ButtonAnimator.applyHoverAndClickEffect(backBtn, 1.05f, 0.95f, () -> {
            ScreenManager.getInstance().pushScreen(new LoginScreen(game));
        });
        baseTable.add(backBtn).height(50).padTop(5).row();

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

    private TextField createField(String hint, boolean isPassword, TextField.TextFieldStyle style) {
        TextField field = new TextField("", style);
        field.setMessageText(hint);
        field.setAlignment(Align.center);
        if (isPassword) {
            field.setPasswordMode(true);
            field.setPasswordCharacter('*');
        }
        return field;
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
