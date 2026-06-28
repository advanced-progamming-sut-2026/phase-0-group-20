package test.java.models;

import models.Result;
import models.database.DataBaseManager;
import models.database.UserRepository;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.users.PasswordUtils;
import models.users.User;
import models.validation.UserValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static models.validation.ValidationConstants.NICKNAME_MAX_LENGTH;
import static models.validation.ValidationConstants.NICKNAME_MIN_LENGTH;
import static org.junit.Assert.*;

public class UserValidationUnitTest {

    private final String TEST_FILE_PATH = "core/test/resources/test_users_validation.json";

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);

        User existingUser = new User(
                "existingUser",
                PasswordUtils.hashPassword("Pass1234!"),
                "Existing",
                "existing@example.com",
                Gender.MALE,
                SecurityQuestion.PET_NAME,
                PasswordUtils.hashPassword("koko")
        );
        DataBaseManager.saveOrUpdateUser(existingUser);
    }

    @After
    public void tearDown() {
        new File(TEST_FILE_PATH).delete();
        DataBaseManager.resetRepositoryToDefault();
    }


    @Test
    public void testValidateUsername_valid() {
        Result result = UserValidator.validateUsername("ali-123");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateUsername_invalidCharPattern() {
        Result result = UserValidator.validateUsername("ali@123");
        assertFalse(result.isSuccessful());
        assertEquals("username can only contain letters, numbers and -", result.message());
    }

    @Test
    public void testValidateUsername_emptyUsername() {
        Result result = UserValidator.validateUsername("");
        assertFalse(result.isSuccessful());
        assertEquals("username cannot be empty", result.message());
    }

    @Test
    public void testValidateUsername_duplicate() {
        Result result = UserValidator.validateUsername("existingUser");
        assertFalse(result.isSuccessful());
        assertEquals("this username already exists", result.message());
    }


    @Test
    public void testValidatePassword_valid() {
        Result result = UserValidator.validatePassword("Secure1!");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidatePassword_emptyPassword() {
        Result result = UserValidator.validatePassword("");
        assertFalse(result.isSuccessful());
        assertEquals("password cannot be empty", result.message());
    }

    @Test
    public void testValidatePassword_tooShort() {
        Result result = UserValidator.validatePassword("Ab1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must be at least 8 characters", result.message());
    }

    @Test
    public void testValidatePassword_noUppercase() {
        Result result = UserValidator.validatePassword("secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain uppercase letters", result.message());
    }

    @Test
    public void testValidatePassword_noLowercase() {
        Result result = UserValidator.validatePassword("SECURE1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain lowercase letters", result.message());
    }

    @Test
    public void testValidatePassword_noNumber() {
        Result result = UserValidator.validatePassword("Secure!!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain numbers", result.message());
    }

    @Test
    public void testValidatePassword_noSpecialChar() {
        Result result = UserValidator.validatePassword("Secure123");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain a special symbol", result.message());
    }


    @Test
    public void testValidateEmail_valid() {
        Result result = UserValidator.validateEmail("user@example.com");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateEmail_empty() {
        Result result = UserValidator.validateEmail("");
        assertFalse(result.isSuccessful());
        assertEquals("email cannot be empty", result.message());
    }

    @Test
    public void testValidateEmail_noAt() {
        Result result = UserValidator.validateEmail("userexample.com");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testValidateEmail_doubleDot() {
        Result result = UserValidator.validateEmail("user..name@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }


    @Test
    public void testValidateNickname_valid() {
        Result result = UserValidator.validateNickname("Ali");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateNickname_empty() {
        Result result = UserValidator.validateNickname("");
        assertFalse(result.isSuccessful());
        assertEquals("nickname cannot be empty", result.message());
    }

    @Test
    public void testValidateNickname_tooShort() {
        Result result = UserValidator.validateNickname("Al");
        assertFalse(result.isSuccessful());
        assertEquals("nickname must be between " + NICKNAME_MIN_LENGTH + " and " + NICKNAME_MAX_LENGTH + " characters",
                result.message());
    }

    @Test
    public void testValidateNickname_tooLong() {
        Result result = UserValidator.validateNickname("A".repeat(31));
        assertFalse(result.isSuccessful());
        assertEquals("nickname must be between " + NICKNAME_MIN_LENGTH + " and " + NICKNAME_MAX_LENGTH + " characters",
                result.message());
    }


    @Test
    public void testValidatePasswordsMatch_matching() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "Secure1!");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidatePasswordsMatch_emptyPassword() {
        Result result = UserValidator.validatePasswordsMatch("", "Secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("password cannot be empty", result.message());
    }

    @Test
    public void testValidatePasswordsMatch_emptyRepeatPassword() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "");
        assertFalse(result.isSuccessful());
        assertEquals("repeat password cannot be empty", result.message());
    }

    @Test
    public void testValidatePasswordsMatch_notMatching() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "Different1!");
        assertFalse(result.isSuccessful());
        assertEquals("password and repeat password don't match", result.message());
    }


    @Test
    public void testValidateAnswersMatch_matching() {
        Result result = UserValidator.validateAnswersMatch("koko", "koko");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateAnswersMatch_emptyAnswer() {
        Result result = UserValidator.validateAnswersMatch("", "koko");
        assertFalse(result.isSuccessful());
        assertEquals("security answer cannot be empty", result.message());
    }

    @Test
    public void testValidateAnswersMatch_emptyRepeatAnswer() {
        Result result = UserValidator.validateAnswersMatch("koko", "");
        assertFalse(result.isSuccessful());
        assertEquals("confirmation answer cannot be empty", result.message());
    }

    @Test
    public void testValidateAnswersMatch_notMatching() {
        Result result = UserValidator.validateAnswersMatch("fluffy", "koko");
        assertFalse(result.isSuccessful());
        assertEquals("security answer and its repetition do not match", result.message());
    }


    @Test
    public void testValidateGender_valid() {
        Result result = UserValidator.validateGender("preferred not to say");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateGender_empty() {
        Result result = UserValidator.validateGender("");
        assertFalse(result.isSuccessful());
        assertEquals("gender cannot be empty. Do you prefer not to say? choose that option",
                result.message());
    }

    @Test
    public void testValidateGender_invalid() {
        Result result = UserValidator.validateGender("HELICOPTER");
        assertFalse(result.isSuccessful());
        assertEquals("gender doesn't exist", result.message());
    }
}