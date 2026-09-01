package io.java.pvz.models.database;

import com.fasterxml.jackson.core.type.TypeReference;
import io.java.pvz.models.users.PasswordUtils;
import io.java.pvz.models.users.User;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Optional;

public class UserRepository extends JsonRepository<User, String> {

    private static final String FILE_PATH = determineFilePath();

    private static String determineFilePath() {
        if (new File("assets").exists()) {
            return "assets/data/users.json";
        }
        return "data/users.json";
    }

    public UserRepository() {
        this(FILE_PATH);
    }

    public UserRepository(String filePath) {
        super(filePath, User::getId, new TypeReference<LinkedHashMap<String, User>>() {
        });
    }

    public Optional<User> findLoggedInUser() {
        return findOne(User::isStayLoggedIn);
    }

    public Optional<User> findByUsername(String username) {
        return findOne(user -> user.getUsername().equals(username));
    }

    public Optional<User> authenticate(String username, String password) {
        if (isLikelySha256(password)) {
            return findByUsername(username).filter(user -> user.getPasswordHash().equals(password));
        } else {
            String inputHash = PasswordUtils.hashPassword(password);
            return findByUsername(username)
                .filter(user -> user.getPasswordHash().equals(inputHash));
        }
    }

    public Optional<User> findForRecovery(String username, String email) {
        return findOne(user -> user.getUsername().equals(username)
            && user.getEmail().equals(email));
    }

    public void logout(String id) {
        findById(id).ifPresent(user -> {
            user.setStayLoggedIn(false);
            save(user);
        });
    }

    public static boolean isLikelySha256(String text) {
        if (text == null) {
            return false;
        }
        return text.matches("^[a-fA-F0-9]{64}$");
    }
}
