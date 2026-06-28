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

import static org.junit.Assert.*;

public class ProfileMenuControllerUnitTest {

    private final String TEST_FILE_PATH = "core/test/resources/test_users_profile.json";
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
    public void testChangeUsername_valid_returnsSuccess() {
        Result result = controller.changeUsername("newUser123");
        assertTrue(result.isSuccessful());
        assertEquals("username has been changed successfully", result.message());
    }

    @Test
    public void testChangeUsername_sameUsername_returnsFailure() {
        Result result = controller.changeUsername("ali123");
        assertFalse(result.isSuccessful());
        assertEquals("username is already in use", result.message());
    }

    @Test
    public void testChangeUsername_invalidPattern_returnsFailure() {
        Result result = controller.changeUsername("invalid@user");
        assertFalse(result.isSuccessful());
        assertEquals("username can only contain letters, numbers and -", result.message());
    }

    @Test
    public void testChangeUsername_duplicateUsername_returnsFailure() {
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
    public void testChangeUsername_noLoggedInUser_returnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeUsername("newUser123");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }


    @Test
    public void testChangePassword_valid_returnsSuccess() {
        Result result = controller.changePassword("Secure1!", "NewPass1/");
        System.out.println(result.message());
        assertTrue(result.isSuccessful());
        assertEquals("password has been changed successfully", result.message());
    }

    @Test
    public void testChangePassword_wrongOldPassword_returnsFailure() {
        Result result = controller.changePassword("wrongPass", "NewPass1-");
        assertFalse(result.isSuccessful());
        assertEquals("password does not match!", result.message());
    }

    @Test
    public void testChangePassword_sameAsOldPassword_returnsFailure() {
        Result result = controller.changePassword("Secure1!", "Secure1!");
        assertFalse(result.isSuccessful());
        assertEquals("Your new password is the same as your old password", result.message());
    }

    @Test
    public void testChangePassword_weakNewPassword_returnsFailure() {
        Result result = controller.changePassword("Secure1!", "weak");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testChangePassword_valid_loginWithNewPasswordWorks() {
        Result result = controller.changePassword("Secure1!", "NewPass1/");
        System.out.println(result.message());

        assertNotNull(DataBaseManager.authenticateUser("ali123", "NewPass1/"));
    }

    @Test
    public void testChangePassword_noLoggedInUser_returnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changePassword("Secure1!", "NewPass1\\");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }


    @Test
    public void testChangeEmail_valid_returnsSuccess() {
        Result result = controller.changeEmail("new@example.com");
        assertTrue(result.isSuccessful());
        assertEquals("email has been changed successfully", result.message());
    }

    @Test
    public void testChangeEmail_sameEmail_returnsFailure() {
        Result result = controller.changeEmail("ali@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("Your new email is the same as your old email", result.message());
    }

    @Test
    public void testChangeEmail_invalidEmail_returnsFailure() {
        Result result = controller.changeEmail("not-an-email");
        assertFalse(result.isSuccessful());
        assertEquals("invalid email address", result.message());
    }

    @Test
    public void testChangeEmail_noLoggedInUser_returnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeEmail("new@example.com");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }


    @Test
    public void testChangeNickname_valid_returnsSuccess() {
        Result result = controller.changeNickname("SuperAli");
        assertTrue(result.isSuccessful());
        assertEquals("nickname has been changed successfully", result.message());
    }

    @Test
    public void testChangeNickname_tooShort_returnsFailure() {
        Result result = controller.changeNickname("Al");
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testChangeNickname_noLoggedInUser_returnsFailure() {
        App.setActiveUser(null);
        Result result = controller.changeNickname("SuperAli");
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }


    @Test
    public void testShowUserInfo_loggedIn_returnsSuccess() {
        Result result = controller.showUserInfo();
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testShowUserInfo_containsUsername() {
        Result result = controller.showUserInfo();
        assertTrue(result.message().contains("ali123"));
    }

    @Test
    public void testShowUserInfo_containsNickname() {
        Result result = controller.showUserInfo();
        assertTrue(result.message().contains("Ali"));
    }

    @Test
    public void testShowUserInfo_noLoggedInUser_returnsFailure() {
        App.setActiveUser(null);
        Result result = controller.showUserInfo();
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }
}