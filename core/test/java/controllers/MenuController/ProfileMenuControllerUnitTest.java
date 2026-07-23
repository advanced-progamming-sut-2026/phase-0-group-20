package test.java.controllers.MenuController;

import controllers.MenuController.ProfileMenuController;
import models.App;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProfileMenuControllerUnitTest {

    private static final String TEST_FILE_PATH = "core/test/resources/test_users_profile.json";
    private ProfileMenuController controller;

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);
        controller = new ProfileMenuController();

        User testUser = new User(
                "ali123",
                PasswordUtils.hashPassword("Secure1!"),
                "Ali",
                "ali@example.com",
                Gender.MALE,
                SecurityQuestion.PET_NAME,
                PasswordUtils.hashPassword("fluffy")
        );
        DataBaseManager.saveOrUpdateUser(testUser);
        App.setActiveUser(testUser);
    }

    @After
    public void tearDown() {
        App.setActiveUser(null);
        new File(TEST_FILE_PATH).delete();
        DataBaseManager.resetRepositoryToDefault();
    }

    @Test
    public void testChangeUsernameValidReturnsSuccess() {
        Result result = controller.changeUsername("newUser123");
        assertTrue(result.isSuccessful());
        assertEquals("username has been changed successfully", result.message());
    }

    @Test
    public void testChangeUsernameSameUsernameReturnsFailure() {
        Result result = controller.changeUsername("ali123");
        assertFalse(result.isSuccessful());
        assertEquals("username is already in use", result.message());
    }

    @Test
    public void testChangeUsernameInvalidPatternReturnsFailure() {
        Result result = controller.changeUsername("invalid@user");
        assertFalse(result.isSuccessful());
        assertEquals("username can only contain letters, numbers and -", result.message());
    }

    @Test
    public void testChangeUsernameDuplicateUsernameReturnsFailure() {
        User otherUser = new User(
                "otherUser",
                PasswordUtils.hashPassword("Secure1!"),
                "Other",
                "other@example.com",
                Gender.MALE,
                SecurityQuestion.PET_NAME,
                PasswordUtils.hashPassword("fluffy")
        );
        DataBaseManager.saveOrUpdateUser(otherUser);

        Result result = controller.changeUsername("otherUser");
        assertFalse(result.isSuccessful());
        assertEquals("this username already exists", result.message());
    }

    @Test
    public void testChangeUsernameNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeUsername("newUser123");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }

    @Test
    public void testChangePasswordValidReturnsSuccess() {
        Result result = controller.changePassword("Secure1!", "NewPass1/");
        assertTrue(result.isSuccessful());
        assertEquals("password has been changed successfully", result.message());
    }

    @Test
    public void testChangePasswordWrongOldPasswordReturnsFailure() {
        Result result = controller.changePassword("wrongPass", "NewPass1-");
        assertFalse(result.isSuccessful());
        assertEquals("password does not match!", result.message());
    }

    @Test
    public void testChangePasswordSameAsOldPasswordReturnsFailure() {
        Result result = controller.changePassword("Secure1!", "Secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("Your new password is the same as your old password", result.message());
    }

    @Test
    public void testChangePasswordWeakNewPasswordReturnsFailure() {
        Result result = controller.changePassword("Secure1!", "weak");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testChangePasswordValidLoginWithNewPasswordWorks() {
        Result result = controller.changePassword("Secure1!", "NewPass1/");
        assertTrue(result.isSuccessful());
        assertNotNull(DataBaseManager.authenticateUser("ali123", "NewPass1/"));
    }

    @Test
    public void testChangePasswordNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changePassword("Secure1!", "NewPass1\\");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }

    @Test
    public void testChangeEmailValidReturnsSuccess() {
        Result result = controller.changeEmail("new@example.com");
        assertTrue(result.isSuccessful());
        assertEquals("email has been changed successfully", result.message());
    }

    @Test
    public void testChangeEmailSameEmailReturnsFailure() {
        Result result = controller.changeEmail("ali@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("Your new email is the same as your old email", result.message());
    }

    @Test
    public void testChangeEmailInvalidEmailReturnsFailure() {
        Result result = controller.changeEmail("not-an-email");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testChangeEmailNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeEmail("new@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }

    @Test
    public void testChangeNicknameValidReturnsSuccess() {
        Result result = controller.changeNickname("SuperAli");
        assertTrue(result.isSuccessful());
        assertEquals("nickname has been changed successfully", result.message());
    }

    @Test
    public void testChangeNicknameTooShortReturnsFailure() {
        Result result = controller.changeNickname("Al");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testChangeNicknameNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeNickname("SuperAli");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }

    @Test
    public void testShowUserInfoLoggedInReturnsSuccess() {
        Result result = controller.showUserInfo();
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testShowUserInfoContainsUsername() {
        Result result = controller.showUserInfo();
        assertTrue(result.message().contains("ali123"));
    }

    @Test
    public void testShowUserInfoContainsNickname() {
        Result result = controller.showUserInfo();
        assertTrue(result.message().contains("Ali"));
    }

    @Test
    public void testShowUserInfoNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.showUserInfo();
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }
}