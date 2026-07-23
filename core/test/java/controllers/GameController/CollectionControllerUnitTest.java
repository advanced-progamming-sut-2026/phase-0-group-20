package test.java.controllers.GameController;

import controllers.GameController.CollectionController;
import models.App;
import models.database.DataBaseManager;
import models.database.UserRepository;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.users.PasswordUtils;
import models.users.User;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public class CollectionControllerUnitTest {

    private static final String TEST_FILE_PATH = "core/test/resources/test_users_collection.json";
    private CollectionController controller;
    private User testUser;

    @Before
    public void setUp() {
        UserRepository testRepo = new UserRepository(TEST_FILE_PATH);
        DataBaseManager.setRepositoryForTest(testRepo);
        controller = new CollectionController();

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


}