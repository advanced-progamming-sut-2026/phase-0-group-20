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

    private static final String TEST_FILE_PATH = "core/test/resources/test_users_validation.json";

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
    public void testValidateUsernameValid() {
        Result result = UserValidator.validateUsername("ali-123");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateUsernameInvalidCharPattern() {
        Result result = UserValidator.validateUsername("ali@123");
        assertFalse(result.isSuccessful());
        assertEquals("username can only contain letters, numbers and -", result.message());
    }

    @Test
    public void testValidateUsernameEmptyUsername() {
        Result result = UserValidator.validateUsername("");
        assertFalse(result.isSuccessful());
        assertEquals("username cannot be empty", result.message());
    }

    @Test
    public void testValidateUsernameDuplicate() {
        Result result = UserValidator.validateUsername("existingUser");
        assertFalse(result.isSuccessful());
        assertEquals("this username already exists", result.message());
    }

    @Test
    public void testValidatePasswordValid() {
        Result result = UserValidator.validatePassword("Secure1!");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidatePasswordEmptyPassword() {
        Result result = UserValidator.validatePassword("");
        assertFalse(result.isSuccessful());
        assertEquals("password cannot be empty", result.message());
    }

    @Test
    public void testValidatePasswordTooShort() {
        Result result = UserValidator.validatePassword("Ab1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must be at least 8 characters", result.message());
    }

    @Test
    public void testValidatePasswordNoUppercase() {
        Result result = UserValidator.validatePassword("secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain uppercase letters", result.message());
    }

    @Test
    public void testValidatePasswordNoLowercase() {
        Result result = UserValidator.validatePassword("SECURE1!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain lowercase letters", result.message());
    }

    @Test
    public void testValidatePasswordNoNumber() {
        Result result = UserValidator.validatePassword("Secure!!");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain numbers", result.message());
    }

    @Test
    public void testValidatePasswordNoSpecialChar() {
        Result result = UserValidator.validatePassword("Secure123");
        assertFalse(result.isSuccessful());
        assertEquals("weak password: must contain a special symbol", result.message());
    }

    @Test
    public void testValidateEmailValid() {
        Result result = UserValidator.validateEmail("user@example.com");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateEmailEmpty() {
        Result result = UserValidator.validateEmail("");
        assertFalse(result.isSuccessful());
        assertEquals("email cannot be empty", result.message());
    }

    @Test
    public void testValidateEmailNoAt() {
        Result result = UserValidator.validateEmail("userexample.com");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testValidateEmailDoubleDot() {
        Result result = UserValidator.validateEmail("user..name@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testValidateNicknameValid() {
        Result result = UserValidator.validateNickname("Ali");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateNicknameEmpty() {
        Result result = UserValidator.validateNickname("");
        assertFalse(result.isSuccessful());
        assertEquals("nickname cannot be empty", result.message());
    }

    @Test
    public void testValidateNicknameTooShort() {
        Result result = UserValidator.validateNickname("Al");
        assertFalse(result.isSuccessful());
        String expectedMessage = "nickname must be between " + NICKNAME_MIN_LENGTH + " and "
                + NICKNAME_MAX_LENGTH + " characters";
        assertEquals(expectedMessage, result.message());
    }

    @Test
    public void testValidateNicknameTooLong() {
        Result result = UserValidator.validateNickname("A".repeat(31));
        assertFalse(result.isSuccessful());
        String expectedMessage = "nickname must be between " + NICKNAME_MIN_LENGTH + " and "
                + NICKNAME_MAX_LENGTH + " characters";
        assertEquals(expectedMessage, result.message());
    }

    @Test
    public void testValidatePasswordsMatchMatching() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "Secure1!");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidatePasswordsMatchEmptyPassword() {
        Result result = UserValidator.validatePasswordsMatch("", "Secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("password cannot be empty", result.message());
    }

    @Test
    public void testValidatePasswordsMatchEmptyRepeatPassword() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "");
        assertFalse(result.isSuccessful());
        assertEquals("repeat password cannot be empty", result.message());
    }

    @Test
    public void testValidatePasswordsMatchNotMatching() {
        Result result = UserValidator.validatePasswordsMatch("Secure1!", "Different1!");
        assertFalse(result.isSuccessful());
        assertEquals("password and repeat password don't match", result.message());
    }

    @Test
    public void testValidateAnswersMatchMatching() {
        Result result = UserValidator.validateAnswersMatch("koko", "koko");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateAnswersMatchEmptyAnswer() {
        Result result = UserValidator.validateAnswersMatch("", "koko");
        assertFalse(result.isSuccessful());
        assertEquals("security answer cannot be empty", result.message());
    }

    @Test
    public void testValidateAnswersMatchEmptyRepeatAnswer() {
        Result result = UserValidator.validateAnswersMatch("koko", "");
        assertFalse(result.isSuccessful());
        assertEquals("confirmation answer cannot be empty", result.message());
    }

    @Test
    public void testValidateAnswersMatchNotMatching() {
        Result result = UserValidator.validateAnswersMatch("fluffy", "koko");
        assertFalse(result.isSuccessful());
        assertEquals("security answer and its repetition do not match", result.message());
    }

    @Test
    public void testValidateGenderValid() {
        Result result = UserValidator.validateGender("preferred not to say");
        assertTrue(result.isSuccessful());
        assertEquals("", result.message());
    }

    @Test
    public void testValidateGenderEmpty() {
        Result result = UserValidator.validateGender("");
        assertFalse(result.isSuccessful());
        assertEquals("gender cannot be empty. Do you prefer not to say? choose that option",
                result.message());
    }

    @Test
    public void testValidateGenderInvalid() {
        Result result = UserValidator.validateGender("HELICOPTER");
        assertFalse(result.isSuccessful());
        assertEquals("gender doesn't exist", result.message());
    }
}