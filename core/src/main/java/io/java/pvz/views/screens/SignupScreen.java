package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.MenuController.SignupMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.models.enums.SecurityQuestion;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class SignupScreen extends BaseScreen {

    private final SignupMenuController signupController;
    private TextureRegion backgroundRegion;
    private final Skin skin;
    private final TextureBank textures;

    private String pendingUsername;
    private String pendingPassword;
    private String pendingRepeatPassword;
    private String pendingNickname;
    private String pendingEmail;
    private String pendingGender;

    public SignupScreen(Game game) {
        super(game);
        skin = AssetLoader.getInstance().getSkin();
        textures = AssetLoader.getInstance().getTextures();

        this.signupController = new SignupMenuController();
        buildUI();
    }

    private void buildUI() {
        Table baseTable = buildBaseTable();
        TextField.TextFieldStyle customFieldStyle = buildStyle();

        TextField usernameField = createField("Username", false, customFieldStyle);
        TextField passwordField = createField("Password", true, customFieldStyle);
        TextField repeatPasswordField = createField("Repeat Password", true, customFieldStyle);
        TextField nicknameField = createField("Nickname", false, customFieldStyle);
        TextField emailField = createField("Email", false, customFieldStyle);
        TextField genderField = createField("Gender (MALE/FEMALE)", false, customFieldStyle);

        baseTable.add(usernameField).height(65).width(350).padBottom(5).row();
        baseTable.add(passwordField).height(65).width(350).padBottom(5).row();
        baseTable.add(repeatPasswordField).height(65).width(350).padBottom(5).row();
        baseTable.add(nicknameField).height(65).width(350).padBottom(5).row();
        baseTable.add(emailField).height(65).width(350).padBottom(5).row();
        baseTable.add(genderField).height(65).width(350).padBottom(15).row();

        TextButton registerBtn = new TextButton("Register", skin, "purple");
        ButtonAnimator.applyHoverAndClickEffect(registerBtn, 1.1f, 0.9f, () -> {
            Result result = signupController.register(usernameField.getText(), passwordField.getText(),
                repeatPasswordField.getText(), nicknameField.getText(), emailField.getText(), genderField.getText()
            );

            if (result.isSuccessful()) {
                pendingUsername = usernameField.getText().trim();
                pendingPassword = passwordField.getText();
                pendingRepeatPassword = repeatPasswordField.getText();
                pendingNickname = nicknameField.getText().trim();
                pendingEmail = emailField.getText().trim();
                pendingGender = genderField.getText().trim();
                showSecurityQuestionsList();
            } else {
                System.out.println("Registration Failed: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Registration Failed: " + result.message())
                        .build());
            }
        });
        baseTable.add(registerBtn).height(70).padTop(15).row();

        TextButton backBtn = UiFactory.textButton("Already have account? Login", skin,
            "green_small", 1.05f, 0.95f, () -> {
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

        BorderedTable centerTable = new BorderedTable();
        centerTable.pad(80);
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

    private void showSecurityQuestionsList() {
        mainLayer.clear();
        modalLayer.clearChildren();
        Skin skin = AssetLoader.getInstance().getSkin();
        BorderedTable popup = new BorderedTable();

        TextButton backBtn = UiFactory.textButton("Back", skin, "green_small", 1.05f, 0.95f, () -> {
            modalLayer.clearChildren();
            buildUI();
        });
        popup.add(backBtn).width(100).height(50).left().padBottom(15).row();

        Label questionTitle = new Label("Select a Security Question", skin);
        questionTitle.setColor(Color.BROWN);
        popup.add(questionTitle).padBottom(20).row();

        SecurityQuestion[] questions = SecurityQuestion.values();
        for (int i = 0; i < questions.length; i++) {
            final String qNumber = String.valueOf(i + 1);
            final String qText = questions[i].getQuestion();

            TextButton qBtn = new TextButton(qText, skin, "green_small");
            ButtonAnimator.applyHoverAndClickEffect(qBtn, 1.02f, 0.98f, () -> {
                showAnswerPopup(qNumber, qText);
            });
            popup.add(qBtn).fillX().padBottom(5).row();
        }

        modalLayer.add(popup).center();
    }

    private void showAnswerPopup(String qNumber, String questionText) {
        modalLayer.clearChildren();
        Skin skin = AssetLoader.getInstance().getSkin();
        TextField.TextFieldStyle style = buildStyle();

        BorderedTable popup = new BorderedTable();

        TextButton backBtn = UiFactory.textButton("Back", skin, "green_small",
            1.05f, 0.95f, this::showSecurityQuestionsList);

        popup.add(backBtn).width(100).height(50).left().padBottom(15).row();

        Label questionLabel = new Label(questionText, skin);
        questionLabel.setColor(Color.BROWN);
        popup.add(questionLabel).padBottom(20).row();

        TextField answerField = new TextField("", style);
        answerField.setMessageText("Your Answer");
        answerField.setAlignment(Align.center);

        TextField confirmField = new TextField("", style);
        confirmField.setMessageText("Confirm Answer");
        confirmField.setAlignment(Align.center);

        TextButton submitBtn = new TextButton("Submit Registration", skin, "purple");
        ButtonAnimator.applyHoverAndClickEffect(submitBtn, 1.1f, 0.9f, () -> {
            Result result = signupController.pickQuestion(qNumber, answerField.getText(), confirmField.getText());
            if (result.isSuccessful()) {
                registerWithServer(qNumber, answerField.getText(), confirmField.getText());
                ScreenManager.getInstance().pushScreen(new LoginScreen(game));
            } else {
                System.out.println("Error: " + result.message());
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Error: " + result.message())
                        .build());
            }
        });

        popup.add(answerField).height(50).width(350).padBottom(10).row();
        popup.add(confirmField).height(50).width(350).padBottom(20).row();
        popup.add(submitBtn).height(60).width(250).row();

        modalLayer.add(popup).center();
    }

    private void registerWithServer(String questionNumber, String answer, String confirmAnswer) {
        if (pendingUsername == null) return;

        NetworkController.getInstance().register(pendingUsername, pendingPassword, pendingRepeatPassword,
            pendingNickname, pendingEmail, pendingGender, registerResponse -> {
                if (registerResponse != null && registerResponse.isSuccess()) {
                    NetworkController.getInstance().pickSecurityQuestion(questionNumber, answer, confirmAnswer,
                        finishResponse -> {
                            if (finishResponse != null && finishResponse.isSuccess()) {
                                System.out.println("Account created on game server: " + pendingUsername);
                                NetworkController.getInstance().login(pendingUsername, pendingPassword, r -> {
                                });
                            } else {
                                System.out.println("Server-side signup step 2 failed: "
                                    + (finishResponse != null ? finishResponse.getErrorMessage()
                                    : "server unreachable"));
                            }
                        });
                } else {
                    NetworkController.getInstance().login(pendingUsername, pendingPassword, loginResponse -> {
                        if (loginResponse == null || !loginResponse.isSuccess()) {
                            System.out.println("Online account unavailable: "
                                + (registerResponse != null ? registerResponse.getErrorMessage()
                                : "server unreachable"));
                        }
                    });
                }
            });
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }
}
