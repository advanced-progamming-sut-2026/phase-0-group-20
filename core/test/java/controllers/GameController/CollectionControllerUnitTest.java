package test.java.controllers.GameController;

import com.Project.PVZ.controllers.GameController.CollectionController;
import com.Project.PVZ.models.App;
import com.Project.PVZ.models.database.DataBaseManager;
import com.Project.PVZ.models.database.UserRepository;
import com.Project.PVZ.models.enums.Gender;
import com.Project.PVZ.models.enums.SecurityQuestion;
import com.Project.PVZ.models.users.PasswordUtils;
import com.Project.PVZ.models.users.User;
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
