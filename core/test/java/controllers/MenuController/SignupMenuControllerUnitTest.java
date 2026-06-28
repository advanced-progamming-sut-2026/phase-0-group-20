package test.java.controllers.MenuController;

import controllers.MenuController.SignupMenuController;
import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.database.UserRepository;
import models.enums.Gender;
import models.enums.Menu;
import models.enums.SecurityQuestion;
import models.users.PasswordUtils;
import models.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class SignupMenuControllerUnitTest {

    private final String TEST_FILE_PATH = "core/test/resources/test_users_signup.json";
    private SignupMenuController controller;

    private final String VALID_USERNAME = "newUser123";
    private final String VALID_PASSWORD = "Secure1!";
    private final String VALID_NICKNAME = "Ali";
    private final String VALID_EMAIL = "ali@example.com";
    private final String VALID_GENDER = "male";

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);
        controller = new SignupMenuController();

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
    public void testRegister_validInputs() {
        Result result = controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        assertTrue(result.isSuccessful());
        assertTrue(result.message().contains("please select and answer one of the following security questions"));
    }


    @Test
    public void testRegister_duplicateUsername() {
        Result result = controller.register("existingUser", VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertEquals("this username already exists", result.message());
    }

    @Test
    public void testRegister_invalidUsername() {
        Result result = controller.register("invalid@user", VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertFalse(result.message().isEmpty());
    }


    @Test
    public void testRegister_weakPassword() {
        Result result = controller.register(VALID_USERNAME, "weak", "weak",
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertTrue(result.message().contains("weak password"));
    }

    @Test
    public void testRegister_passwordMismatch() {
        Result result = controller.register(VALID_USERNAME, VALID_PASSWORD, "Different1!",
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertEquals("password and repeat password don't match", result.message());
    }


    @Test
    public void testRegister_invalidEmail() {
        Result result = controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, "not-an-email", VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testRegister_invalidNickname() {
        Result result = controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                "Al", VALID_EMAIL, VALID_GENDER);

        assertFalse(result.isSuccessful());
        assertTrue(result.message().contains("nickname must be between"));
    }

    @Test
    public void testRegister_invalidGender() {
        Result result = controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, "HELICOPTER");

        assertFalse(result.isSuccessful());
        assertEquals("gender doesn't exist", result.message());
    }

    @Test
    public void testRegister_trimsWhitespace() {
        Result result = controller.register("  " + VALID_USERNAME + "  ", VALID_PASSWORD, VALID_PASSWORD,
                "  " + VALID_NICKNAME + "  ", "  " + VALID_EMAIL + "  ", "  " + VALID_GENDER + "  ");

        assertTrue(result.isSuccessful());
        assertTrue(result.message().contains("security questions"));
    }


    @Test
    public void testPickQuestion_validAfterRegister() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("1", "myAnswer", "myAnswer");

        assertTrue(result.isSuccessful());
        assertEquals("registration was successful, you can now login to your account", result.message());
    }

    @Test
    public void testPickQuestion_validAfterRegister_userSavedInDB() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("1", "myAnswer", "myAnswer");

        assertTrue(result.isSuccessful());
        assertTrue(DataBaseManager.usernameExists(VALID_USERNAME));
    }

    @Test
    public void testPickQuestion_validAfterRegister_correctMessage() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("1", "myAnswer", "myAnswer");

        assertTrue(result.isSuccessful());
        assertEquals("registration was successful, you can now login to your account", result.message());
    }


    @Test
    public void testPickQuestion_withoutRegisterFirst() {
        Result result = controller.pickQuestion("1", "myAnswer", "myAnswer");

        assertFalse(result.isSuccessful());
        assertEquals("you must complete the registration process first", result.message());
    }

    @Test
    public void testPickQuestion_answerMismatch() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("1", "myAnswer", "different");

        assertFalse(result.isSuccessful());
        assertEquals("security answer and its repetition do not match", result.message());
    }

    @Test
    public void testPickQuestion_invalidQuestionNumber() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("999", "myAnswer", "myAnswer");

        assertFalse(result.isSuccessful());
        assertEquals("invalid question number", result.message());
    }

    @Test
    public void testPickQuestion_nonNumericQuestion() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result result = controller.pickQuestion("abc", "myAnswer", "myAnswer");

        assertFalse(result.isSuccessful());
        assertEquals("invalid question number", result.message());
    }

    @Test
    public void testPickQuestion_afterFailedAttempt_pendingStillValid() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result failedResult = controller.pickQuestion("1", "myAnswer", "different");
        assertFalse(failedResult.isSuccessful());

        Result successResult = controller.pickQuestion("1", "myAnswer", "myAnswer");
        assertTrue(successResult.isSuccessful());
        assertEquals("registration was successful, you can now login to your account", successResult.message());
    }


    @Test
    public void testPickQuestion_validAfterRegister_changesMenuToLogin() {
        App.setActiveMenu(Menu.SIGNUP_MENU);

        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);
        Result result = controller.pickQuestion("1", "myAnswer", "myAnswer");

        assertTrue(result.isSuccessful());
        assertEquals(Menu.LOGIN_MENU, App.getActiveMenu());
    }


    @Test
    public void testRegister_nullInputs_doesNotCrashAndReturnsFailure() {
        Result result = controller.register(null, null, null,
                null, null, null);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testPickQuestion_invalidQuestionNumber_returnsFailure() {
        controller.register(VALID_USERNAME, VALID_PASSWORD, VALID_PASSWORD,
                VALID_NICKNAME, VALID_EMAIL, VALID_GENDER);

        Result resultZero = controller.pickQuestion("0", "myAnswer", "myAnswer");
        assertFalse(resultZero.isSuccessful());
        assertEquals("invalid question number", resultZero.message());

        Result resultNegative = controller.pickQuestion("-1", "myAnswer", "myAnswer");
        assertFalse(resultNegative.isSuccessful());
        assertEquals("invalid question number", resultNegative.message());
    }

}
