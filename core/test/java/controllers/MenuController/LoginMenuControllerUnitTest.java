package test.java.controllers.MenuController;

import controllers.MenuController.LoginMenuController;
import models.Result;
import models.database.DataBaseManager;
import models.database.UserRepository;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.users.PasswordUtils;
import models.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class LoginMenuControllerUnitTest {

    private final String TEST_FILE_PATH = "core/test/resources/test_users_login.json";
    private LoginMenuController controller;
    private User testUser;

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);
        controller = new LoginMenuController();

        testUser = new User(
                "ali123",
                PasswordUtils.hashPassword("Secure1!"),
                "Ali",
                "ali@example.com",
                Gender.MALE,
                SecurityQuestion.PET_NAME,
                PasswordUtils.hashPassword("fluffy")
        );
        DataBaseManager.saveOrUpdateUser(testUser);
    }

    @After
    public void tearDown() {
        new File(TEST_FILE_PATH).delete();
        DataBaseManager.resetRepositoryToDefault();
    }


    @Test
    public void testLogin_validCredentials_returnsSuccess() {
        Result result = controller.login("ali123", "Secure1!", false);
        assertTrue(result.isSuccessful());
        assertEquals("welcome Ali", result.message());
    }

    @Test
    public void testLogin_wrongPassword_returnsFailure() {
        Result result = controller.login("ali123", "wrongPass", false);
        assertFalse(result.isSuccessful());
        assertEquals("incorrect password", result.message());
    }

    @Test
    public void testLogin_nonExistentUsername_returnsFailure() {
        Result result = controller.login("ghost", "Secure1!", false);
        assertFalse(result.isSuccessful());
        assertEquals("username does not exist", result.message());
    }

    @Test
    public void testLogin_stayLoggedIn_userPersistedAsLoggedIn() {
        controller.login("ali123", "Secure1!", true);
        assertNotNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testLogin_notStayLoggedIn_userNotPersistedAsLoggedIn() {
        controller.login("ali123", "Secure1!", false);
        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testLogin_trimsUsernameWhitespace_returnsSuccess() {
        Result result = controller.login("  ali123  ", "Secure1!", false);
        assertTrue(result.isSuccessful());
        assertEquals("welcome Ali", result.message());
    }


    @Test
    public void testForgetPassword_validUsernameAndEmail_returnsSecurityQuestion() {
        Result result = controller.forgetPassword("ali123", "ali@example.com");
        assertTrue(result.isSuccessful());
        assertEquals(testUser.getSecurityQuestion().getQuestion(), result.message());
    }

    @Test
    public void testForgetPassword_wrongEmail_returnsFailure() {
        Result result = controller.forgetPassword("ali123", "wrong@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("user doesn't exist", result.message());
    }

    @Test
    public void testForgetPassword_nonExistentUser_returnsFailure() {
        Result result = controller.forgetPassword("ghost", "ghost@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("user doesn't exist", result.message());
    }


    @Test
    public void testCheckSecurityQuestion_correctAnswer_returnsSuccess() {
        controller.forgetPassword("ali123", "ali@example.com");
        Result result = controller.checkSecurityQuestion("fluffy");
        assertTrue(result.isSuccessful());
        assertEquals("correct answer, please enter your new password and repeat it", result.message());
    }

    @Test
    public void testCheckSecurityQuestion_wrongAnswer_returnsFailure() {
        controller.forgetPassword("ali123", "ali@example.com");
        Result result = controller.checkSecurityQuestion("wrongAnswer");
        assertFalse(result.isSuccessful());
        assertEquals("incorrect answer, please start over", result.message());
    }

    @Test
    public void testCheckSecurityQuestion_withoutForgetPasswordFirst_returnsFailure() {
        Result result = controller.checkSecurityQuestion("fluffy");
        assertFalse(result.isSuccessful());
        assertEquals("invalid command", result.message());
    }

    @Test
    public void testCheckSecurityQuestion_wrongAnswerClearsPending_blocksFurtherAttempt() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("wrongAnswer");

        Result result = controller.checkSecurityQuestion("fluffy");
        assertFalse(result.isSuccessful());
        assertEquals("invalid command", result.message());
    }


    @Test
    public void testResetPassword_validNewPassword_returnsSuccess() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        Result result = controller.resetPassword("NewPass1!", "NewPass1!");
        assertTrue(result.isSuccessful());
        assertEquals("password changed successfully, you can now login", result.message());
    }

    @Test
    public void testResetPassword_validNewPassword_loginWithNewPasswordWorks() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        controller.resetPassword("NewPass1!", "NewPass1!");

        Result result = controller.login("ali123", "NewPass1!", false);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testResetPassword_validNewPassword_oldPasswordNoLongerWorks() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        controller.resetPassword("NewPass1!", "NewPass1!");

        Result result = controller.login("ali123", "Secure1!", false);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testResetPassword_passwordMismatch_returnsFailure() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        Result result = controller.resetPassword("NewPass1!", "Different1!");
        assertFalse(result.isSuccessful());
        assertEquals("password and repeat password don't match", result.message());
    }

    @Test
    public void testResetPassword_weakPassword_returnsFailure() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        Result result = controller.resetPassword("weak", "weak");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testResetPassword_withoutSecurityCheckFirst_returnsFailure() {
        Result result = controller.resetPassword("NewPass1!", "NewPass1!");
        assertFalse(result.isSuccessful());
        assertEquals("invalid command", result.message());
    }

    @Test
    public void testResetPassword_calledTwice_secondCallFails() {
        controller.forgetPassword("ali123", "ali@example.com");
        controller.checkSecurityQuestion("fluffy");
        controller.resetPassword("NewPass1!", "NewPass1!");

        Result result = controller.resetPassword("AnotherPass1!", "AnotherPass1!");
        assertFalse(result.isSuccessful());
        assertEquals("invalid command", result.message());
    }
}