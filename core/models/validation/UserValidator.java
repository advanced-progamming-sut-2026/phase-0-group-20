package models.validation;

import models.Result;
import models.database.DataBaseManager;
import models.enums.Gender;

import static models.validation.ValidationConstants.*;

public final class UserValidator {

    private UserValidator() {
    }

    public static Result validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches())
            return new Result(false, "username can only contain letters, numbers and -");
        if (DataBaseManager.usernameExists(username))
            return new Result(false, "this username already exists");
        return new Result(true, "");
    }

    public static Result validatePassword(String password) {
        if (password.length() < PASSWORD_MIN_LENGTH)
            return new Result(false, "weak password: must be at least " + PASSWORD_MIN_LENGTH + " characters");
        if (!password.matches(".*[a-z].*"))
            return new Result(false, "weak password: must contain lowercase letters");
        if (!password.matches(".*[A-Z].*"))
            return new Result(false, "weak password: must contain uppercase letters");
        if (!password.matches(".*[0-9].*"))
            return new Result(false, "weak password: must contain numbers");
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches())
            return new Result(false, "weak password: must contain a special symbol");
        return new Result(true, "");
    }

    public static Result validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches())
            return new Result(false, "invalid email address");
        return new Result(true, "");
    }

    public static Result validateNickname(String nickname) {
        if (nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH)
            return new Result(false, "nickname must be between " +
                    NICKNAME_MIN_LENGTH + " and " + NICKNAME_MAX_LENGTH + " characters");
        return new Result(true, "");
    }

    public static Result validatePasswordsMatch(String password, String repeatPassword) {
        if (!password.equals(repeatPassword))
            return new Result(false, "password and repeat password don't match");
        return new Result(true, "");
    }

    public static Result validateAnswersMatch(String answer, String confirmAnswer) {
        if (!answer.equals(confirmAnswer))
            return new Result(false, "security answer and its repetition do not match");
        return new Result(true, "");
    }

    public static Result validateGender(String gender) {
        if (Gender.findByValue(gender) == null)
            return new Result(false, "gender do not exist");
        return new Result(true, "");
    }
}