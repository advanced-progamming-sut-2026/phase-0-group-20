package models.validation;

import java.util.regex.Pattern;

public final class ValidationConstants {

    private ValidationConstants() {
    }

    public static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    public static final Pattern SPECIAL_CHAR_PATTERN =
            Pattern.compile(".*[!#$%^&*)(=+\\]\\[{}|/\\\\:;'\",<>?].*");

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?!.*\\.\\.)[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]@" +
                    "(?!-)[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int NICKNAME_MIN_LENGTH = 3;
    public static final int NICKNAME_MAX_LENGTH = 30;
}