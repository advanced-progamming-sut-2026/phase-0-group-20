package controllers.MenuController;

import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.enums.Gender;
import models.enums.Menu;
import models.enums.SecurityQuestion;
import models.quest.Quest;
import models.quest.QuestLoader;
import models.users.PasswordUtils;
import models.users.PendingRegistration;
import models.users.User;
import models.validation.UserValidator;

import java.util.List;

public class SignupMenuController {

    private PendingRegistration pendingRegistration;

    public Result register(String username, String password, String repeatPassword,
                           String nickname, String email, String genderStr) {

        if (username != null) username = username.trim();
        if (nickname != null) nickname = nickname.trim();
        if (email != null) email = email.trim();
        if (genderStr != null) genderStr = genderStr.trim();


        Result validation = validateRegistration(username, password, repeatPassword, nickname, email, genderStr);
        if (!validation.isSuccessful())
            return validation;

        Gender gender = Gender.findByValue(genderStr);
        pendingRegistration = new PendingRegistration(
                username, PasswordUtils.hashPassword(password), nickname, email, gender);

        return new Result(true, buildSecurityQuestionsList());
    }

    private Result validateRegistration(String username, String password, String repeatPassword,
                                        String nickname, String email, String genderStr) {

        Result result;

        if (!(result = UserValidator.validateUsername(username)).isSuccessful()) return result;
        if (!(result = UserValidator.validatePassword(password)).isSuccessful()) return result;
        if (!(result = UserValidator.validatePasswordsMatch(password, repeatPassword)).isSuccessful()) return result;
        if (!(result = UserValidator.validateNickname(nickname)).isSuccessful()) return result;
        if (!(result = UserValidator.validateEmail(email)).isSuccessful()) return result;
        if (!(result = UserValidator.validateGender(genderStr)).isSuccessful()) return result;

        return new Result(true, "");
    }

    private String buildSecurityQuestionsList() {
        StringBuilder sb = new StringBuilder("please select and answer one of the following security questions:\n");
        SecurityQuestion[] questions = SecurityQuestion.values();
        for (int i = 0; i < questions.length; i++)
            sb.append(i + 1).append(". ").append(questions[i].getQuestion()).append("\n");
        return sb.toString();
    }

    public Result pickQuestion(String questionNumberStr, String answer, String confirmAnswer) {
        if (pendingRegistration == null)
            return new Result(false, "you must complete the registration process first");

        Result matchResult = UserValidator.validateAnswersMatch(answer, confirmAnswer);
        if (!matchResult.isSuccessful())
            return matchResult;

        SecurityQuestion securityQuestion = resolveSecurityQuestion(questionNumberStr);
        if (securityQuestion == null)
            return new Result(false, "invalid question number");

        User newUser = new User(
                pendingRegistration.username(),
                pendingRegistration.passwordHash(),
                pendingRegistration.nickname(),
                pendingRegistration.email(),
                pendingRegistration.gender(),
                securityQuestion,
                PasswordUtils.hashPassword(answer)
        );
        loadInitialQuestsForUser(newUser);
        DataBaseManager.saveOrUpdateUser(newUser);
        pendingRegistration = null;
        App.setActiveMenu(Menu.LOGIN_MENU);
        return new Result(true, "registration was successful, you can now login to your account");
    }

    private SecurityQuestion resolveSecurityQuestion(String questionNumberStr) {
        int questionNumber;
        try {
            questionNumber = Integer.parseInt(questionNumberStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        SecurityQuestion[] questions = SecurityQuestion.values();
        if (questionNumber < 1 || questionNumber > questions.length)
            return null;
        return questions[questionNumber - 1];
    }

    private void loadInitialQuestsForUser(User user) {
        List<Quest> loadedQuests = QuestLoader.loadQuestsFromJson("phase-0-group-20/assets/quests.json");
        for (Quest q : loadedQuests) {
            user.getQuestManager().addQuest(q);
        }
    }
}