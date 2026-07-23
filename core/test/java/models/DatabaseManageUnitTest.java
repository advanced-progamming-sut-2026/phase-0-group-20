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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DatabaseManageUnitTest {

    private static final String TEST_FILE_PATH = "core/test/resources/test_users.json";
    private UserRepository testRepo;
    private User testUser;

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
    public void testSaveUserUserCanBeRetrievedAfterSave() {
        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull("User should exist after saveOrUpdateUser", fetched);
        assertEquals("ali123", fetched.getUsername());
    }

    @Test
    public void testSaveUserUpdateExistingUserOverwritesCorrectly() {
        testUser.setNickname("AliUpdated");
        DataBaseManager.saveOrUpdateUser(testUser);

        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(fetched);
        assertEquals("AliUpdated", fetched.getNickname());
    }

    @Test
    public void testSaveUserDoesNotDuplicateUser() {
        DataBaseManager.saveOrUpdateUser(testUser);
        int countAfterFirstSave = testRepo.findAll().size();

        DataBaseManager.saveOrUpdateUser(testUser);
        int countAfterSecondSave = testRepo.findAll().size();

        assertTrue(DataBaseManager.usernameExists("ali123"));
        assertNotNull(DataBaseManager.authenticateUser("ali123", "pass1234"));

        assertEquals("Repetition in saving same user shouldn't increase user count",
                countAfterFirstSave, countAfterSecondSave);
    }

    @Test
    public void testAuthenticateUserCorrectCredentialsReturnsUser() {
        User result = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(result);
        assertEquals("ali123", result.getUsername());
    }

    @Test
    public void testAuthenticateUserWrongPasswordReturnsNull() {
        assertNull(DataBaseManager.authenticateUser("ali123", "wrongpass"));
    }

    @Test
    public void testAuthenticateUserWrongUsernameReturnsNull() {
        assertNull(DataBaseManager.authenticateUser("nobody", "pass1234"));
    }

    @Test
    public void testAuthenticateUserEmptyCredentialsReturnsNull() {
        assertNull(DataBaseManager.authenticateUser("", ""));
    }

    @Test
    public void testGetLoggedInUserNoOneLoggedInReturnsNull() {
        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testGetLoggedInUserUserLoggedInReturnsCorrectUser() {
        testUser.setStayLoggedIn(true);
        DataBaseManager.saveOrUpdateUser(testUser);

        User loggedIn = DataBaseManager.getLoggedInUser();
        assertNotNull(loggedIn);
        assertEquals("ali123", loggedIn.getUsername());
    }

    @Test
    public void testLogoutUserClearsStayLoggedIn() {
        testUser.setStayLoggedIn(true);
        DataBaseManager.saveOrUpdateUser(testUser);

        DataBaseManager.logoutUser(testUser.getId());

        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testLogoutUserNonExistentIdDoesNotThrow() {
        DataBaseManager.logoutUser("non-existent-id-999");
        assertNull(DataBaseManager.getLoggedInUser());
    }

    @Test
    public void testGetUserForRecoveryCorrectUsernameAndEmailReturnsUser() {
        User result = DataBaseManager.getUserForRecovery("ali123", "ali@example.com");
        assertNotNull(result);
        assertEquals("ali123", result.getUsername());
    }

    @Test
    public void testGetUserForRecoveryWrongEmailReturnsNull() {
        assertNull(DataBaseManager.getUserForRecovery("ali123", "wrong@example.com"));
    }

    @Test
    public void testGetUserForRecoveryWrongUsernameReturnsNull() {
        assertNull(DataBaseManager.getUserForRecovery("ghost", "ali@example.com"));
    }

    @Test
    public void testUpdateForgotPasswordNewPasswordWorksForLogin() {
        DataBaseManager.updateForgotPassword("ali123", PasswordUtils.hashPassword("newpass99"));
        assertNotNull(DataBaseManager.authenticateUser("ali123", "newpass99"));
    }

    @Test
    public void testUpdateForgotPasswordOldPasswordNoLongerWorks() {
        DataBaseManager.updateForgotPassword("ali123", PasswordUtils.hashPassword("newpass99"));
        assertNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }

    @Test
    public void testUpdateForgotPasswordNonExistentUserDoesNotThrow() {
        DataBaseManager.updateForgotPassword("ghost_user", PasswordUtils.hashPassword("x"));
    }

    @Test
    public void testUpdateUsernameReturnsTrue() {
        assertTrue(DataBaseManager.updateUsername(testUser, "ali_new"));
    }

    @Test
    public void testUpdateUsernameNewUsernameExistsInDB() {
        DataBaseManager.updateUsername(testUser, "ali_new");
        assertTrue(DataBaseManager.usernameExists("ali_new"));
    }

    @Test
    public void testUpdateUsernameOldUsernameNoLongerExists() {
        DataBaseManager.updateUsername(testUser, "ali_new");
        assertFalse(DataBaseManager.usernameExists("ali123"));
    }

    @Test
    public void testUpdateEmailNewEmailWorksForRecovery() {
        DataBaseManager.updateEmail(testUser, "new@example.com");
        assertNotNull(DataBaseManager.getUserForRecovery("ali123", "new@example.com"));
    }

    @Test
    public void testUpdateEmailOldEmailNoLongerWorksForRecovery() {
        DataBaseManager.updateEmail(testUser, "new@example.com");
        assertNull(DataBaseManager.getUserForRecovery("ali123", "ali@example.com"));
    }

    @Test
    public void testUpdateNicknameNicknameChangedSuccessfully() {
        DataBaseManager.updateNickname(testUser, "SuperAli");

        User fetched = DataBaseManager.authenticateUser("ali123", "pass1234");
        assertNotNull(fetched);
        assertEquals("SuperAli", fetched.getNickname());
    }

    @Test
    public void testUpdatePasswordCorrectOldPasswordReturnsTrue() {
        assertTrue(DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99"));
    }

    @Test
    public void testUpdatePasswordWrongOldPasswordReturnsFalse() {
        assertFalse(DataBaseManager.updatePassword(testUser, "wrongOld", "newSecure99"));
    }

    @Test
    public void testUpdatePasswordAfterUpdateNewPasswordWorks() {
        DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99");
        assertNotNull(DataBaseManager.authenticateUser("ali123", "newSecure99"));
    }

    @Test
    public void testUpdatePasswordAfterUpdateOldPasswordFails() {
        DataBaseManager.updatePassword(testUser, "pass1234", "newSecure99");
        assertNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }

    @Test
    public void testUpdatePasswordWrongOldPasswordPasswordUnchanged() {
        DataBaseManager.updatePassword(testUser, "wrongOld", "newSecure99");
        assertNotNull(DataBaseManager.authenticateUser("ali123", "pass1234"));
    }

    @Test
    public void testUsernameExistsExistingUsernameReturnsTrue() {
        assertTrue(DataBaseManager.usernameExists("ali123"));
    }

    @Test
    public void testUsernameExistsNonExistingUsernameReturnsFalse() {
        assertFalse(DataBaseManager.usernameExists("ghost_user"));
    }
}