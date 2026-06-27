package test.java.models;

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

public class DatabaseManageUnitTest {

    private final String TEST_FILE_PATH = "core/test/resources/test_users.json";
    private User testUser;
    UserRepository testRepo;

    @Before
    public void setUp() throws Exception {
        testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);

        testUser = new User(
                "ali123",
                PasswordUtils.hashPassword("pass1234"),
                "Ali",
                "ali@example.com",
                Gender.MALE,
                SecurityQuestion.PET_NAME,
                PasswordUtils.hashPassword("koko")
        );

        DataBaseManager.saveOrUpdateUser(testUser);
    }

    @After
    public void tearDown() throws Exception {
        new File(TEST_FILE_PATH).delete();
        DataBaseManager.resetRepositoryToDefault();
    }


    @Test
    public void testSaveUser_userCanBeRetrievedAfterSave() {
        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull("User should exist after saveOrUpdateUser", fetched);
        assertEquals("ali123", fetched.getUsername());
    }

    @Test
    public void testSaveUser_updateExistingUser_overwritesCorrectly() {
        testUser.setNickname("AliUpdated");
        DataBaseManager.saveOrUpdateUser(testUser);

        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(fetched);
        assertEquals("AliUpdated", fetched.getNickname());
    }

    @Test
    public void testSaveUser_doesNotDuplicateUser() {

        DataBaseManager.saveOrUpdateUser(testUser);
        int countAfterFirstSave = testRepo.findAll().size();

        DataBaseManager.saveOrUpdateUser(testUser);
        int countAfterSecondSave = testRepo.findAll().size();

        assertTrue(DataBaseManager.usernameExists("ali123"));

        assertNotNull(DataBaseManager.authenticateUser("ali123", "pass1234"));

        assertEquals("repetition in saving same user shouldn't increase user count",
                countAfterFirstSave, countAfterSecondSave);
    }


    @Test
    public void testAuthenticateUser_correctCredentials_returnsUser() {
        User result = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(result);
        assertEquals("ali123", result.getUsername());
    }

    @Test
    public void testAuthenticateUser_wrongPassword_returnsNull() {
        assertNull(DataBaseManager.authenticateUser("ali123", "wrongpass"));
    }

    @Test
    public void testAuthenticateUser_wrongUsername_returnsNull() {
        assertNull(DataBaseManager.authenticateUser("nobody", "pass1234"));
    }

    @Test
    public void testAuthenticateUser_emptyCredentials_returnsNull() {
        assertNull(DataBaseManager.authenticateUser("", ""));
    }


    @Test
    public void testGetLoggedInUser_noOneLoggedIn_returnsNull() {
        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testGetLoggedInUser_userLoggedIn_returnsCorrectUser() {
        testUser.setStayLoggedIn(true);
        DataBaseManager.saveOrUpdateUser(testUser);

        User loggedIn = DataBaseManager.getLoggedInUser();
        assertNotNull(loggedIn);
        assertEquals("ali123", loggedIn.getUsername());
    }


    @Test
    public void testLogoutUser_clearsStayLoggedIn() {
        testUser.setStayLoggedIn(true);
        DataBaseManager.saveOrUpdateUser(testUser);

        DataBaseManager.logoutUser(testUser.getId());

        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testLogoutUser_nonExistentId_doesNotThrow() {
        DataBaseManager.logoutUser("non-existent-id-999");
        assertNull(DataBaseManager.getLoggedInUser());
    }


    @Test
    public void testGetUserForRecovery_correctUsernameAndEmail_returnsUser() {
        User result = DataBaseManager.getUserForRecovery("ali123", "ali@example.com");
        assertNotNull(result);
        assertEquals("ali123", result.getUsername());
    }

    @Test
    public void testGetUserForRecovery_wrongEmail_returnsNull() {
        assertNull(DataBaseManager.getUserForRecovery("ali123", "wrong@example.com"));
    }

    @Test
    public void testGetUserForRecovery_wrongUsername_returnsNull() {
        assertNull(DataBaseManager.getUserForRecovery("ghost", "ali@example.com"));
    }


    @Test
    public void testUpdateForgotPassword_newPasswordWorksForLogin() {
        DataBaseManager.updateForgotPassword("ali123", PasswordUtils.hashPassword("newpass99"));
        assertNotNull(DataBaseManager.authenticateUser("ali123", "newpass99"));
    }

    @Test
    public void testUpdateForgotPassword_oldPasswordNoLongerWorks() {
        DataBaseManager.updateForgotPassword("ali123", PasswordUtils.hashPassword("newpass99"));
        assertNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }

    @Test
    public void testUpdateForgotPassword_nonExistentUser_doesNotThrow() {
        // assert no exception
        DataBaseManager.updateForgotPassword("ghost_user", PasswordUtils.hashPassword("x"));
    }


    @Test
    public void testUpdateUsername_returnsTrue() {
        assertTrue(DataBaseManager.updateUsername(testUser, "ali_new"));
    }

    @Test
    public void testUpdateUsername_newUsernameExistsInDB() {
        DataBaseManager.updateUsername(testUser, "ali_new");
        assertTrue(DataBaseManager.usernameExists("ali_new"));
    }

    @Test
    public void testUpdateUsername_oldUsernameNoLongerExists() {
        DataBaseManager.updateUsername(testUser, "ali_new");
        assertFalse(DataBaseManager.usernameExists("ali123"));
    }


    @Test
    public void testUpdateEmail_newEmailWorksForRecovery() {
        DataBaseManager.updateEmail(testUser, "new@example.com");
        assertNotNull(DataBaseManager.getUserForRecovery("ali123", "new@example.com"));
    }

    @Test
    public void testUpdateEmail_oldEmailNoLongerWorksForRecovery() {
        DataBaseManager.updateEmail(testUser, "new@example.com");
        assertNull(DataBaseManager.getUserForRecovery("ali123", "ali@example.com"));
    }


    @Test
    public void testUpdateNickname_nicknameChangedSuccessfully() {
        DataBaseManager.updateNickname(testUser, "SuperAli");

        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(fetched);
        assertEquals("SuperAli", fetched.getNickname());
    }


    @Test
    public void testUpdatePassword_correctOldPassword_returnsTrue() {
        assertTrue(DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99"));
    }

    @Test
    public void testUpdatePassword_wrongOldPassword_returnsFalse() {
        assertFalse(DataBaseManager.updatePassword(testUser, "wrongOld", "newSecure99"));
    }

    @Test
    public void testUpdatePassword_afterUpdate_newPasswordWorks() {
        DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99");
        assertNotNull(DataBaseManager.authenticateUser("ali123", "newSecure99"));
    }

    @Test
    public void testUpdatePassword_afterUpdate_oldPasswordFails() {
        DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99");
        assertNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }

    @Test
    public void testUpdatePassword_wrongOldPassword_passwordUnchanged() {
        DataBaseManager.updatePassword(testUser, "wrongOld", "newSecure99");
        assertNotNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }


    @Test
    public void testUsernameExists_existingUsername_returnsTrue() {
        assertTrue(DataBaseManager.usernameExists("ali123"));
    }

    @Test
    public void testUsernameExists_nonExistingUsername_returnsFalse() {
        assertFalse(DataBaseManager.usernameExists("ghost_user"));
    }
}