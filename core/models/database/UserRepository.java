package models.database;

import com.fasterxml.jackson.core.type.TypeReference;
import models.users.PasswordUtils;
import models.users.User;


import java.util.LinkedHashMap;
import java.util.Optional;


public class UserRepository extends JsonRepository<User, String> {

    private static final String FILE_PATH = "data/users.json";

    public UserRepository() {
        super(FILE_PATH, User::getId, new TypeReference<LinkedHashMap<String, User>>() {});
    }

    public Optional<User> findLoggedInUser() {
        return findOne(User::isStayLoggedIn);
    }

    public Optional<User> findByUsername(String username) {
        return findOne(user -> user.getUsername().equals(username));
    }

    public Optional<User> authenticate(String username, String password) {
        String inputHash = PasswordUtils.hashPassword(password);
        return findByUsername(username)
                .filter(user -> user.getPasswordHash().equals(inputHash));
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
}