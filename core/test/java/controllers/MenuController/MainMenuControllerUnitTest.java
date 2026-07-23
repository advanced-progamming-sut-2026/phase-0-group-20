package test.java.controllers.MenuController;

import controllers.MenuController.MainMenuController;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MainMenuControllerUnitTest {

    private static final String TEST_FILE_PATH = "core/test/resources/test_users_main.json";
    private MainMenuController controller;
    private User testUser;

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);
        controller = new MainMenuController();

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
        App.setActiveUser(testUser);
    }

    @After
    public void tearDown() {
        App.setActiveUser(null);
        new File(TEST_FILE_PATH).delete();
        DataBaseManager.resetRepositoryToDefault();
    }

    @Test
    public void testLogoutLoggedInUserReturnsSuccess() {
        Result result = controller.logout();
        assertTrue(result.isSuccessful());
        assertEquals("logged out successfully", result.message());
    }

    @Test
    public void testLogoutClearsActiveUser() {
        controller.logout();
        assertNull(App.getActiveUser());
    }

    @Test
    public void testLogoutSetsMenuToLoginMenu() {
        controller.logout();
        assertEquals(Menu.LOGIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testLogoutClearsStayLoggedIn() {
        testUser.setStayLoggedIn(true);
        DataBaseManager.saveOrUpdateUser(testUser);

        controller.logout();

        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testLogoutNoLoggedInUserReturnsFailure() {
        App.setActiveUser(null);
        Result result = controller.logout();
        assertFalse(result.isSuccessful());
        assertEquals("no user is currently logged in", result.message());
    }
}